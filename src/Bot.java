import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class Bot extends ListenerAdapter {
    
    private static JDA api;

    private static ArrayList<Long> admins = new ArrayList<>();
    
    static Guild guild;
    private static TextChannel controlChannel; 
    static boolean joined;

    static int votesSkip;
    static ArrayList<String> usersVotedForSkip = new ArrayList<>();

    public static void start() {
        	try {
				if(!Config.load()) {
					Log.print("Failed to load config.");
		    		a.errExit();
				}
        		
				if(Config.get(Config.BOT_TOKEN).isEmpty()) {
					Log.print("You must specify a Token in the config file or as argument! You can get it here: https://discordapp.com/developers/applications/me");
					a.errExit();
				}
				
				String adms = Config.get(Config.ADMIN_IDS);
				if(!adms.isEmpty() && !adms.startsWith("#")) {
					String[] admsArr = adms.split(":");
					for(String admin : admsArr) {
						try {
							admins.add(Long.parseLong(admin));
						} catch(NumberFormatException e) {
							Log.print("Invalid admin ID: " + admin);
						}
					}
				}
				
				Log.print("Starting JDA ...");
				
                api = new JDABuilder(AccountType.BOT).setToken(Config.get(Config.BOT_TOKEN))
                		//.setEnableShutdownHook(false) // default: true
                		.buildBlocking();
                api.addEventListener(new Bot());
                
                // old shutdown hook
                /*Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
					@Override
					public void run() {
						Log.printWithoutNL("Shutting down ...");
						api.shutdown();
						Log.printRawWithNL(" Bye");	
					}
				}));*/
                
                // test for only one server
                int guilds = api.getGuilds().size();
                
                if(guilds == 0) {
                    //Log.print("To add the bot to your server, please visit: " + api.asBot().getInviteUrl(Permission.ADMINISTRATOR)); // start with permissions
                    Log.print("To add the bot to your server, please visit: " + api.asBot().getInviteUrl());
                	a.errExit();
                	
                } else if(guilds > 1) {
                	Log.print("This bot is on more than 1 server. This is currently not supported.");
                	a.errExit();
                }
                
                guild = api.getGuilds().get(0);
                
                try {
                	controlChannel = guild.getTextChannelsByName(Config.get(Config.CONTROL_CHANNEL), true).get(0); // true for Ignore Case
                } catch(IndexOutOfBoundsException e) {
                	Log.print("There is no '" + Config.get(Config.CONTROL_CHANNEL) + "' Text Channel.");
                	//TODO: Test shutdown
                	a.errExit();
                }
                
                // Init Player
                PlayerThread.init();
                
                // Start game update thread
                if(Boolean.valueOf(Config.get(Config.DISPLAY_SONG_AS_GAME))) {
                    new Thread(new PlayerThread()).start();	
                }
                
                Log.print("Successfully started.");
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    static void shutdown() {
		Log.print("Shutting down ...");
		//api.shutdown(); // done by shutdown hook of JDA
        System.exit(0);
    }
    
    static JDA getApi() {
        return api;
    }

    static void leave() {
    	// only defined guild, for one server
        guild.getAudioManager().closeAudioConnection();
        joined = false;
    }
    
    private static boolean isAdmin(User user) {
    	return user.getId().equals(guild.getOwner().getUser().getId()) || admins.contains(Long.parseLong(user.getId()));
    }
    
    
	@SuppressWarnings("unused")
	@Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        String message = event.getMessage().getContent();
        
        if ( (event.getChannel() == controlChannel) && message.startsWith(Config.get(Config.COMMAND_PREFIX)) ) {

        	String[] cmdarg = message.substring(Config.get(Config.COMMAND_PREFIX).length()).split(" ", 2);
        	String cmd = cmdarg[0].toLowerCase();
        	String arg;
        	try {
        		arg = cmdarg[1];
        	} catch (IndexOutOfBoundsException e) {
				arg = null;
			}
        	
        	switch (cmd) {
			case "help":
				event.getChannel().sendMessage(event.getAuthor().getAsMention() + " **Commands:**\n"
						+ "\n**Everyone:**" // "\n" not here
						+ "```\n" // after "```", don't ask me why
						+ "list\n"
						+ "id"
						+ "```"
						+ "\n**Admins:**\n"
						+ "```"
						+ "play <file, folder or link>\n"
						+ "pause\n"
						+ "skip <how many songs>\n"
						+ "repeat <how many times>\n"
						+ "stop\n"
						+ "version\n"
						+ "kill"
						+ "```").queue();
				
				break;
				
			case "kill":
                if(isAdmin(event.getAuthor())) {
    				event.getChannel().sendMessage("Bye").complete(); // complete(): block this thread (send the message first, than shutdown)
                    shutdown();
                } else {
                	event.getChannel().sendMessage(event.getAuthor().getAsMention() + " ``Only admins may kill me.``").queue();
                }
				
				break;
				
			case "skip":
				if(PlayerThread.isPlaying()) {
					User author = event.getMessage().getAuthor();
					
	                if(isAdmin(event.getAuthor())) {
	                	
	                	int skips;
	                	if(arg == null) {
	                		skips = 1;
	                	} else {
	        				try {
	        					skips = Integer.valueOf(arg);
	        					if(skips < 1) {
	        						throw new NumberFormatException();
	        					}
	        				} catch(NumberFormatException e) {
	        					event.getChannel().sendMessage(event.getAuthor().getAsMention() +  " Invalid number").queue();
	        					return;
	        				}
	                	}
	                	
	                	PlayerThread.getMusicManager().scheduler.nextTrack(skips);
	                } else {
	                	event.getChannel().sendMessage(event.getAuthor().getAsMention() + " ``Only admins may skip.``").queue();
	                }
					
                } else {
                    event.getChannel().sendMessage(event.getAuthor().getAsMention() + " ``Currently I'm not playing.``").queue();
                }
				
				break;
				
			case "repeat":
                if(isAdmin(event.getAuthor())) {
    		
    				int repeats;
                	if(arg == null) {
                		repeats = 1;
                	} else {
        				try {
        					repeats = Integer.valueOf(arg);
        					if(repeats < 1) {
        						throw new NumberFormatException();
        					}
        				} catch(NumberFormatException e) {
        					event.getChannel().sendMessage(event.getAuthor().getAsMention() +  " Invalid number").queue();
        					return;
        				}
                	}
                	
                	if(PlayerThread.isPlaying()) {
                    	
                    	ArrayList<AudioTrack> songs = new ArrayList<>();
                    	songs.add(PlayerThread.getMusicManager().player.getPlayingTrack());
                    	ArrayList<AudioTrack> upcoming = PlayerThread.getMusicManager().scheduler.getList();
                    	if(!upcoming.isEmpty()) {
                    		for(int i = 0; i < upcoming.size(); i++) {
                    			songs.add(upcoming.get(i));
                    		}
                    	}
                		
                    	for(int i = 0; i < repeats; i++) {
                    		for(int j = 0; j < songs.size(); j++) {
                        		PlayerThread.play(songs.get(j).makeClone());
                        	}
                    	}
                    	
                    	event.getChannel().sendMessage("``Repeated the playlist " + repeats + " times.``").queue();
                	} else {
                		event.getChannel().sendMessage(event.getAuthor().getAsMention() +  " ``The playlist is empty. There is nothing to repeat.``").queue();
                	}
    				
                } else {
                	event.getChannel().sendMessage(event.getAuthor().getAsMention() + " ``Sorry, only admins may use the repeat command.``").queue();
                }
				
				break;

			case "list":
				PlayerThread.sendPlaylist(event.getAuthor(), event.getChannel());
				
				break;
				
			case "pause":
				if(isAdmin(event.getAuthor())) {
					if(PlayerThread.isPaused()) {
						event.getChannel().sendMessage("Continue playback ...").queue();
						PlayerThread.setPaused(false);
					} else {
						PlayerThread.setPaused(true);
						event.getChannel().sendMessage("Paused").queue();
					}
                } else {
                	event.getChannel().sendMessage(event.getAuthor().getAsMention() + " ``Only admins may pause me.``").queue();
                }
				
				break;
				
			case "stop":
                if(isAdmin(event.getAuthor())) {
    				// stop the music
                	PlayerThread.stop();
    				// leave the channel
    				leave();
    				// cancel skipping
    				PlayerThread.skipping = false;
    				// clear the playlist
    				PlayerThread.getMusicManager().scheduler.clear();
                } else {
                	event.getChannel().sendMessage(event.getAuthor().getAsMention() + " ``Only admins may stop me.``").queue();
                }
				
				break;
				
			case "id":
				event.getAuthor().getPrivateChannel().sendMessage("Your (admin-)ID: " + event.getAuthor().getId()).queue();
				
				break;
				
			case "version":
				event.getChannel().sendMessage(event.getAuthor().getAsMention() + "\n"
						+ "``"
						+ Values.BOT_NAME + ": " + Values.BOT_VERSION
						+ "\n"
						+ "JDA: " + JDAInfo.VERSION
						+ "\n"
						+ "Lavaplayer: " + PlayerLibrary.VERSION
						+ "``").queue();
				
				break;
				
			case "play":
                if(isAdmin(event.getAuthor())) {
                	
    				if(arg == null) {
                		event.getChannel().sendMessage("Please specify what I should play. Put it behind this command.").queue();
                		return;
                	}
                	
                	if (!joined) {
	                    VoiceChannel channel = event.getGuild().getVoiceChannels().stream().filter(vChan -> vChan.getName().equalsIgnoreCase(Config.get(Config.VOICE_CHANNEL))).findFirst().orElse(null);
	                    try {
	                    	event.getGuild().getAudioManager().openAudioConnection(channel);
	                        joined = true;
	                    } catch(NullPointerException e) {
	                    	Log.print("Failed to join Voice Channel.");
	                    }
	                }
                	
	                if(joined) {

	                	File inputFile = new File(arg);
                        if(inputFile.isDirectory()) {
                        	event.getChannel().sendMessage("Adding folder to queue: " + inputFile).queue();
                        	File[] files = inputFile.listFiles();
                        	Arrays.sort(files);
                        	for(File f : files) {
                        		if(f.isFile()) {
                        			PlayerThread.loadAndPlay(event.getChannel(), f.getAbsolutePath(), true);
                        		}
                        	}
                        } else {
                        	PlayerThread.loadAndPlay(event.getChannel(), arg, false);
                        }
                        
	                }
	                
                } else {
                	event.getChannel().sendMessage(event.getAuthor().getAsMention() + " ``Sorry, only admins may play something.``").queue();
                }
				
				break;
				
				
			default:
				event.getChannel().sendMessage(event.getAuthor().getAsMention() + " ``Unknown command``").queue();
				break;
			}
             
        }
    }
	
	static void setGame(Game game) {
		api.getPresence().setGame(game);
		//System.out.println("GAME UPDATE: " + game);
	}
	
	static String getTrackName(AudioTrack track) {
		String sourceName = track.getSourceManager().getSourceName();
		if(sourceName.equals("local") || sourceName.equals("http")) {
			return track.getIdentifier();
		} else {
			return track.getInfo().title;
		}
	}

}

