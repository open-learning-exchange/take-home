package pbell.offline.ole.org.pbell;

import android.util.Log;

/**
 * Created by leonardmensah on 18/05/16.
 */
public class ListModel {

    private  String ResourceTitle="";
    private  String Image="";
    private  String Description="";

    /*********** Set Methods ******************/

    public void setTitle(String ResourceTitle)
    {
        this.ResourceTitle = ResourceTitle;
    }

    public void setImage(String Image)
    {
        this.Image = Image;
    }

    public void setDescription(String Description)
    {

        Log.e("MYAPP", "DD "+Description);
        this.Description = Description;
    }

    /*********** Get Methods ****************/

    public String getTitle()
    {
        return this.ResourceTitle;
    }

    public String getImage()
    {
        return this.Image;
    }

    public String getDescription()
    {
        return this.Description;
    }


}