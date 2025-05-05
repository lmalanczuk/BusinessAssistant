//package com.licencjat.BusinessAssistant.platform.zoom;
//
//import com.licencjat.BusinessAssistant.client.zoom.ZoomMeetingClient;
//import com.licencjat.BusinessAssistant.client.zoom.ZoomRecordingClient;
//import com.licencjat.BusinessAssistant.platform.MeetingPlatform;
//import com.licencjat.BusinessAssistant.platform.MeetingPlatformClient;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//@Component
//public class ZoomPlatform implements MeetingPlatform {
//    private final ZoomMeetingClient zoomMeetingClient;
//    private final ZoomRecordingClient zoomRecordingClient;
//
//    @Autowired
//    public ZoomPlatform(ZoomMeetingClient zoomMeetingClient, ZoomRecordingClient zoomRecordingClient) {
//        this.zoomMeetingClient = zoomMeetingClient;
//        this.zoomRecordingClient = zoomRecordingClient;
//    }
//
//    @Override
//    public String getPlatformId() {
//        return "ZOOM";
//    }
//
//    @Override
//    public MeetingPlatformClient createClient() {
//        return new ZoomPlatformClient(zoomMeetingClient, zoomRecordingClient);
//    }
//}