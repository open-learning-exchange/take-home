package pbell.offline.ole.org.pbell;

import java.util.ArrayList;

/**
 * Created by leonardmensah on 21/07/16.
 */
@SuppressWarnings("ALL")
public class CourseList {

        private String title;
        private int thumbnailUrl;
        private String rating;
        private String descr;
        private ArrayList<String> genre;

        public CourseList() {

        }

        public CourseList(String name, int thumbnailUrl, String rating, String descr,
                        ArrayList<String> genre) {
            this.title = name;
            this.thumbnailUrl = thumbnailUrl;
            this.rating = rating;
            this.descr = descr;
            this.genre = genre;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String name) {
            this.title = name;
        }

        public int getThumbnailUrl() {
            return thumbnailUrl;
        }

        public void setThumbnailUrl(int thumbnailUrl) {
            this.thumbnailUrl = thumbnailUrl;
        }

        public String getRating() {
            return rating;
        }

        public void setRating(String rating) {
            this.rating = rating;
        }

        public String getDescription() {
            return descr;
        }

        public void setDescription(String descr) {
            this.descr = descr;
        }

        public ArrayList<String> getGenre() {
            return genre;
        }

        public void setGenre(ArrayList<String> genre) {
            this.genre = genre;
        }

}
