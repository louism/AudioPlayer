package audio;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 *
 * @author Louis
 * 
 * Main class for the audio player.
 */
public class AudioPlayer extends Application 
{
    
    int seconds = 0;
    long lastTime = 0;
    int duration;
    boolean paused;
    boolean fading;
    boolean unknown;
    
    BorderPane main;
    Stage primaryStage;
    Scene primaryScene;
    VBox controls;
    AudioPlayer audioPlayer;
    ArrayList<String> playlist = new ArrayList<>();
    
    ImageView albumArt;
    ProgressBar progressBar;
    MediaPlayer currentPlayer;
    MediaPlayer fadeIn;
    MediaPlayer fadeOut;
    Label trackLabel;
    Playlist currentPlaylist;
    Random r = new Random();
    
    @Override
    public void start(Stage primaryStage) 
    {
        //Setup the main interface
        main = new BorderPane();
        this.audioPlayer = this;
        this.primaryStage = primaryStage;
        albumArt = new ImageView();
        primaryScene = new Scene(main, 180, 195);
        primaryScene.getStylesheets().clear();
        primaryScene.getStylesheets().add("style.css");
        controls = new VBox();
        albumArt.setFitHeight(150);
        albumArt.setFitWidth(150);
        progressBar = new ProgressBar();
        progressBar.setPrefWidth(150);
        progressBar.setScaleY(0.5);
       

        main.setCenter(albumArt);
        main.setBottom(controls);
        trackLabel = new Label("Audio Player");
        trackLabel.setAlignment(Pos.CENTER);
        controls.setAlignment(Pos.CENTER);
        primaryStage.setTitle("Audio Player");
        primaryStage.setScene(primaryScene);
        primaryStage.show();
        
        controls.setPadding(new Insets(0, 5, 0, 5));
        controls.getChildren().addAll(trackLabel, progressBar);

        
       
        
        this.primaryStage.setOnCloseRequest(e-> close());
        loadPlaylist("lib.m3u");
        setUpMenu();
        setUpControls();
        
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    /**
     * Loads the default playlist or opens a open file dialog to choose one.
     */
    private void loadPlaylist(String p)
    {
                
            File ft = new File(p);
            File f = null;
            if(!ft.exists())
            {
            FileChooser fc = new FileChooser();
            f = fc.showOpenDialog(this.primaryStage);
            }
            else
            {
                f = ft;
            }
            openM3U(f);
            Collections.shuffle(playlist);
            openMP3(randomFromLibrary());
        
    }
    /**
     * Sets up controls for the GUI.
     */
    private void setUpControls() 
    {
        albumArt.setOnMouseClicked(e->
        {
            if(!paused)
            {
            paused = true;
            currentPlayer.pause();
            if(fadeIn !=null)
            {
                fadeIn.pause();
            }
            }
            else
            {
                paused = false;
                currentPlayer.play();
                            if(fadeIn !=null)
            {
                fadeIn.play();
            }
                    
            }
        });
    }
    /**
     * Extracts data from the chosen mp3 file using mp3agic.
     */
    private Image extractFromMp3(File f)
    {
                  
        try {
            Mp3File mp3file = new Mp3File(f);
            if (mp3file.hasId3v2Tag())
            {
                
                ID3v2 id3v2Tag = mp3file.getId3v2Tag();
                String album = mp3file.getId3v2Tag().getAlbum();
                String artist = mp3file.getId3v2Tag().getArtist();
                String trackname = mp3file.getId3v2Tag().getTitle();
                trackLabel.setText(artist + " - " + trackname);
                File maybe = new File(album + ".jpg");
                if(maybe.exists())
                {
                    return new Image(new File(album + ".jpg" ).toURI().toString());
                }
                byte[] imageData = id3v2Tag.getAlbumImage();
                if (imageData != null) {
                    
                    // Write image to file - can determine appropriate file extension from the mime type
                    RandomAccessFile file = new RandomAccessFile(album + ".jpg" , "rw");
                    file.write(imageData);
                    file.close();
                    return new Image(new File(album + ".jpg" ).toURI().toString());
                    

                    
                }
            }   } catch (IOException | UnsupportedTagException | InvalidDataException ex) {
            Logger.getLogger(AudioPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }
    return null;
    }


    
        private void openMP3(File f)
    {
        progressBar.setProgress(0);
                    Media media = new Media(f.toURI().toString());
                fadeIn = new MediaPlayer(media);
                 

                fadeIn.setOnReady(() -> {
                    fadeIn.play();
                    fadeIn.setVolume(0);
                    
                    if(fadeIn.getMedia().getDuration()==Duration.UNKNOWN)
                    {
                        duration=10000;
                        unknown = true;
                    }
                    else
                    {
                        duration =  (int) (fadeIn.getMedia().getDuration().toSeconds() /  (fadeIn.getRate()));
                    }
                    seconds=0;
                    currentPlayer= fadeIn;
                    progressBar();
        });
                          albumArt.setImage(extractFromMp3(f));
                          
    }
    
    /**
     * Controls incrementing of progress bar and fading.
     */
    private void progressBar() 
    {
             AnimationTimer timer2 = new AnimationTimer(){             
          @Override
        public void handle(long now) 
        {
             if(!paused)
                    {
            if (lastTime != 0) 
            {
                if (now > lastTime + 1_000_000_000) 
                {
                   if(unknown)
                   {
                     if(fadeIn.getMedia().getDuration()==Duration.UNKNOWN)
                     {
                         duration=100;
                         unknown = true;
                     }
                     else
                     {
                        duration =  (int) (fadeIn.getMedia().getDuration().toSeconds() /  (fadeIn.getRate()));
                        unknown=false;
                     }
                   }

                                    if(fadeIn != null && fadeIn.getVolume()<0.2)
                    {
                    fadeIn.setVolume(fadeIn.getVolume()+0.01);
                    }
                    if(fading && fadeOut!= null)
                    {
                        fadeOut.setVolume(fadeOut.getVolume()-0.01);
                        if(fadeOut.getVolume()<=0)
                        {
                            fadeOut.stop();
                            fading = false;
                        }
                    }
                    seconds++;
     
                    progressBar.setProgress((double)seconds/duration);
                    lastTime = now;
                    if(progressBar.getProgress()>0.95)
                    {
                        openMP3(randomFromLibrary());
                        fadeOut = currentPlayer;
                        fading = true;
                    }
                }

            } else {
                lastTime = now;

            }
        }
        }
        @Override
        public void stop() {
            super.stop();
            lastTime = 0;
            seconds = 0;
        }
    };
       
      timer2.start();
    }


    private File randomFromLibrary()
    {
        String get = playlist.get(0);
        playlist.remove(get);
        if(currentPlaylist!=null)
        {
        currentPlaylist.rm();
        }
       return new File(get);
               
    }
    
    public void play(String path)
    {
        fadeOut = currentPlayer;
        fading = true;
        openMP3(new File(path));
        
    }
    
    public ArrayList<String> getLibrary()
    {
        return playlist;
    }
    
        private void close() 
    {
        File lastFile = new File("library.lst");
        if(!lastFile.exists())
        {
            try {
                lastFile.createNewFile();
            } catch (IOException ex) {
         
            }
        }
        FileWriter fw = null;
        try {
            fw = new FileWriter(lastFile);
        } catch (IOException ex) {

        }
        PrintWriter pw = new PrintWriter(fw);
        for(String s: playlist)
        {
            pw.write(s + "\n");
        }
        pw.close();
    }

/**
 * Shows a small menu on right click.
 */        

private void setUpMenu()
{
    
                ContextMenu cm = new ContextMenu();
MenuItem cmItem1 = new MenuItem("Change Playlist");
MenuItem cmItem2 = new MenuItem("Show Library");

cmItem1.setOnAction((ActionEvent e) -> {
    DirectoryChooser fc = new DirectoryChooser();
    File f = fc.showDialog(primaryStage);
                });
cmItem2.setOnAction((ActionEvent e) -> {
    try {
        if(currentPlaylist == null)
        {
            Playlist library = new Playlist(audioPlayer);
            library.show();
            currentPlaylist=library;
        }
        else
        {
            currentPlaylist.show();
        }
    } catch (       IOException | UnsupportedTagException | InvalidDataException ex) {
        Logger.getLogger(AudioPlayer.class.getName()).log(Level.SEVERE, null, ex);
    }       });

cm.getItems().add(cmItem1);
cm.getItems().add(cmItem2);

                main.setOnMouseClicked((MouseEvent e) -> {
                    if(e.getButton() == MouseButton.SECONDARY)
                    {
                        cm.show(primaryStage, e.getScreenX(), e.getScreenY());

                    }
                });
            }

/**
 * Reads a playlist from the m3u format.
 **/
    private void openM3U(File rand) 
    {
       File lastFile = rand;
        if(lastFile.exists())
        {
            FileReader fr = null;
            try {
                fr = new FileReader(lastFile);
                BufferedReader br = new BufferedReader(fr);
                String line = br.readLine();
                while(line!=null)
                {
                    if(!line.startsWith("#"))
                    {
                    playlist.add(line);

                    
                    }
                    line = br.readLine();
                }
                File rand2 = randomFromLibrary();


            } catch (FileNotFoundException ex) {

            } catch (IOException ex) {

            } finally {
                try {
                    fr.close();
                } catch (IOException ex) {

                }
            }
        }
        
    }


}

    
    
    
    

