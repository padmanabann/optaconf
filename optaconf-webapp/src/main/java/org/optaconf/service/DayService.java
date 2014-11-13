package org.optaconf.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.optaconf.cdi.ScheduleManager;
import org.optaconf.domain.Day;
import org.optaconf.domain.Room;
import org.optaconf.domain.Schedule;
import org.optaconf.domain.Talk;
import org.optaconf.domain.Timeslot;

@Path("/{conferenceId}/day")
public class DayService {

    @Inject
    private ScheduleManager scheduleManager;

    @GET
    @Path("/")
    @Produces("application/json")
    public List<Day> getDayList(@PathParam("conferenceId") Long conferenceId) {
        Schedule schedule = scheduleManager.getSchedule();
        return schedule.getDayList();
    }
    @GET
    @Path("/{dayId}/timeslot")
    @Produces("application/json")
    public List<Timeslot> getTimeslotList(@PathParam("conferenceId") Long conferenceId,
            @PathParam("dayId") String dayId) {
        Schedule schedule = scheduleManager.getSchedule();
        // TODO do proper query to DB instead of filtering here
        List<Timeslot> globalTimeslotList = schedule.getTimeslotList();
        List<Timeslot> timeslotList = new ArrayList<Timeslot>(globalTimeslotList.size());
        for (Timeslot timeslot : globalTimeslotList) {
            if (timeslot.getDay().getId().equals(dayId)) {
                timeslotList.add(timeslot);
            }
        }
        return timeslotList;
    }

    @GET
    @Path("/{dayId}/talk")
    @Produces("application/json")
    public Map<Timeslot, Map<Room, Talk>> getTimeslotRoomToTalkMap(@PathParam("conferenceId") Long conferenceId,
            @PathParam("dayId") String dayId) {
        Schedule schedule = scheduleManager.getSchedule();
        Map<Timeslot, Map<Room, Talk>> timeslotRoomToTalkMap = new LinkedHashMap<Timeslot, Map<Room, Talk>>();
        List<Timeslot> timeslotList = getTimeslotList(conferenceId, dayId);
        for (Timeslot timeslot : timeslotList) {
            Map<Room, Talk> roomToTalkMap = new LinkedHashMap<Room, Talk>();
            List<Room> roomList = schedule.getRoomList();
            for (Room room : roomList) {
                Talk talk = null;
                // TODO refactor this performance drain
                for (Talk selectedTalk : schedule.getTalkList()) {
                    if (selectedTalk.getTimeslot() == timeslot && selectedTalk.getRoom() == room) {
                        talk = selectedTalk;
                        break;
                    }
                }
                roomToTalkMap.put(room, talk);
            }
            timeslotRoomToTalkMap.put(timeslot, roomToTalkMap);
        }
        return timeslotRoomToTalkMap;
    }


}
