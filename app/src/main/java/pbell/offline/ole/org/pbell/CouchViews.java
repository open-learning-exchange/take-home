package pbell.offline.ole.org.pbell;

import android.util.Log;

import com.couchbase.lite.Database;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.View;

import java.util.Map;

/**
 * Created by leonardmensah on 23/05/16.
 */
public class CouchViews {
    public View CreateLoginByIdView(Database db) {
        View LoginByIdView = db.getView("MembersByLoginID");
            LoginByIdView.setMap(
                    new Mapper(){
                        @Override
                        public void map(Map<String, Object> document,Emitter emitter) {
                            if ("Member".equals(document.get("kind"))) {
                                emitter.emit((String) document.get("login"), (String) document.get("_id"));
                            }
                        }
                    }, "9"
            );
        return LoginByIdView;
    }
    public View CreateListByNameView(Database db) {
        View ListByNameView = db.getView("MembersByNameView");
        ListByNameView.setMap(
                new Mapper(){
                    @Override
                    public void map(Map<String, Object> document,Emitter emitter) {
                    if ("Member".equals(document.get("kind"))) {
                            emitter.emit((String) document.get("firstName"), (String) document.get("_id"));
                    }
                    }
                }, "4"
        );
        return ListByNameView;
    }
    public View ReadShelfByIdView(Database db) {
        View shelfListByIdView = db.getView("ShelfByID");
        shelfListByIdView.setMap(
                new Mapper(){
                    @Override
                    public void map(Map<String, Object> document,Emitter emitter) {
                    emitter.emit((String) document.get("memberId"), (String) document.get("_id"));
                    }
                }, "4"
        );
        return shelfListByIdView;
    }
    public View ReadCourseList(Database db) {
        View CourseListByMemberIdView = db.getView("CourseByMemberID");
        CourseListByMemberIdView.setMap(
                new Mapper(){
                    @Override
                    public void map(Map<String, Object> document,Emitter emitter) {
                        emitter.emit((String) document.get("memberId"), (String) document.get("_id"));
                    }
                }, "1"
        );
        return CourseListByMemberIdView;
    }
    public View ReadCourses(Database db) {
        View Courses = db.getView("Courses");
        Courses.setMap(
                new Mapper(){
                    @Override
                    public void map(Map<String, Object> document,Emitter emitter) {
                        emitter.emit((String) document.get("CourseTitle"), (String) document.get("_id"));
                    }
                }, "2"
        );
        return Courses;
    }
    public View ReadCourseSteps(Database db) {
        View CourseSteps = db.getView("Coursestep");
        CourseSteps.setMap(
                new Mapper(){
                    @Override
                    public void map(Map<String, Object> document,Emitter emitter) {
                        emitter.emit((String) document.get("courseId"), (String) document.get("_id"));
                    }
                }, "1"
        );
        return CourseSteps;
    }
    public View ReadMemberVisits(Database db) {
        View VisitsByMemberIdView = db.getView("visits");
        VisitsByMemberIdView.setMap(
                new Mapper(){
                    @Override
                    public void map(Map<String, Object> document,Emitter emitter) {
                        emitter.emit((String) document.get("memberId"), (String) document.get("_id"));
                    }
                }, "1"
        );
        return VisitsByMemberIdView;
    }
    public View ReadMemberVisitsId(Database db) {
        View VisitsMemberVisitsIdView = db.getView("visits");
        VisitsMemberVisitsIdView.setMap(
                new Mapper(){
                    @Override
                    public void map(Map<String, Object> document,Emitter emitter) {
                        emitter.emit((String) document.get("_id"), (String) document.get("_id"));
                    }
                }, "4"
        );
        return VisitsMemberVisitsIdView;
    }
    public View LocalServerInfo(Database db) {
        View ServerInfoView = db.getView("name");
        ServerInfoView.setMap(
                new Mapper(){
                    @Override
                    public void map(Map<String, Object> document,Emitter emitter) {
                        emitter.emit((String) document.get("name"), (String) document.get("_id"));
                    }
                }, "2"
        );
        return ServerInfoView;
    }
    public View ReadResourceRatingByIdView(Database db) {
        View ResourceRatingByIdView = db.getView("ResourceRatingById");
        ResourceRatingByIdView.setMap(
                new Mapper(){
                    @Override
                    public void map(Map<String, Object> document,Emitter emitter) {
                        emitter.emit((String) document.get("_id"), (String) document.get("_id"));
                    }
                }, "11"
        );
        return ResourceRatingByIdView;
    }
    public View ReadActivityLogById(Database db) {
        View ReadActivityLogByIdView = db.getView("ActivityLogById");
        ReadActivityLogByIdView.setMap(
                new Mapper(){
                    @Override
                    public void map(Map<String, Object> document,Emitter emitter) {
                        emitter.emit((String) document.get("_id"), (String) document.get("_id"));
                    }
                }, "3"
        );
        return ReadActivityLogByIdView;
    }
}
