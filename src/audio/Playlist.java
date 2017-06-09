package audio;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

/**
 *
 * @author Louis
 * 
 * Class for the playlist viewer.
 */
public class Playlist
{
    ArrayList<String> library = new ArrayList<String>();
    ArrayList<String> linkLibrary;
    ListView <String>lv;
    AudioPlayer dp;
    
    public Playlist(AudioPlayer dp) throws IOException, UnsupportedTagException, InvalidDataException
    {
        this.dp = dp;
        Thread loadThread = new Thread()
        {
            public void run()
            {
                        linkLibrary = dp.getLibrary();
        for(String link: linkLibrary)
        {
            try{
            Mp3File mp3file = new Mp3File(new File(link));
            if (mp3file.hasId3v2Tag())
            {
                String disp = mp3file.getId3v2Tag().getArtist() + " - " + mp3file.getId3v2Tag().getTitle();
                library.add(disp);
            }
            }catch(Exception e)
            {
                library.add("Unknown file.");
            }
            Platform.runLater(new Runnable()
            {

                @Override
                public void run() {
                    if(lv!=null)
                    {
                      lv.setItems(FXCollections.observableArrayList(library));
                    }
                }
                    
                
            });
        }
            
            }
        };
        loadThread.start();
        
        

    }
    
    public void show()
    {

        Stage stage = new Stage();

        lv = new ListView <>();
        
        Scene libraryScene = new Scene(lv, 300, 780);
              

        stage.setScene(libraryScene);
        stage.show();
        lv.setOnMouseClicked(e->{
        if(e.getClickCount()==2)
        {
            int i = lv.getSelectionModel().getSelectedIndex();
            dp.play(linkLibrary.get(i));
        }
        });
        lv.setItems(FXCollections.observableArrayList(library));
        

    }
    
    public void rm()
    {
        linkLibrary.remove(0);
        library.remove(0);
        lv.setItems(FXCollections.observableArrayList(library));
    }
    
    
}
