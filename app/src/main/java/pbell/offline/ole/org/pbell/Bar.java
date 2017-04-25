package pbell.offline.ole.org.pbell;

/**
 * Created by leonardmensah on 07/03/2017.
 */

import org.lightcouch.Document;

@SuppressWarnings("ALL")
public class Bar extends Document {
    private String bar;

    public Bar() {

    }

    public Bar(String id) {
        this.setId(id);
    }

    public String getBar() {
        return bar;
    }

    public void setBar(String bar) {
        this.bar = bar;
    }

    @Override
    public String toString() {
        return "Bar [bar=" + bar + "]";
    }
}