package org.calendar.api;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.*;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/* class to demonstrate use of Google Calendar APIS */
public class Main {
    /**
     * Application name.
     */
    private static final String APPLICATION_NAME = "Google Calendar API Java";
    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    /**
     * Directory to store authorization tokens for this application.
     */
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_EVENTS);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = Main.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        //returns an authorized Credential object.
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static void main(String... args) throws IOException, GeneralSecurityException, ParseException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();


        /**
         * these are some operation that can be done on Events on the calendar.
         **/

        /* List the next 10 events from the primary calendar.*/
        listEvent(service);
//        getEvent(service,"09epo6tmb6si9nbu940l9c8nfg");
//          createEvent(service);
        /**
         * these are some operation that can be done on the Calendar List .
         **/

        /* Iterate through calendars in calendar list.*/
//        iterateOverCalendersInCalendarList(service);

        /* Create new Calendar and insert it*/
//        createNewCalendarInCalendarList(service);

        /* update Calendar in calendar list*/
//        updateCalendarInCalendarList(service);

        /* delete Calendar in calendar list*/
//        deleteCalendarInCalendarList(service,"classroom112692469518496037151@group.calendar.google.com");
    }

    private static void getEvent(Calendar service, String eventId) throws IOException {
        // Retrieve an event
        Event event = service.events().get("primary", eventId).execute();

        System.out.println(event);
    }

    private static void createEvent(Calendar service) throws IOException {
        Event event = new Event()
                .setSummary("Adeeb's engagement")
                .setLocation("Bashari villa,cairo,egypt")
                .setDescription("Celebrating with our friends and Adeeb about this wonderful event.");

        DateTime startDateTime = new DateTime("2022-07-14T16:00:00Z");
        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone("Africa/Cairo");
        event.setStart(start);

        DateTime endDateTime = new DateTime("2022-07-14T23:00:00Z");
        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone("Africa/Cairo");
        event.setEnd(end);

        String calendarId = "primary";
        event = service.events().insert(calendarId, event).execute();
        System.out.printf("Event created: %s\n", event.getHtmlLink());
    }

    public static void listEvent(@NotNull Calendar service) throws IOException {
        DateTime now = new DateTime(System.currentTimeMillis());
        Events events = service.events().list("primary")
                .setMaxResults(10)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        List<Event> items = events.getItems();
        if (items.isEmpty()) {
            System.out.println("No upcoming events found.");
        } else {
            System.out.println("Upcoming events");
            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    start = event.getStart().getDate();
                }
                System.out.printf("%s (%s)\n", event.getSummary(), start);
                System.out.println("and it's ID:" +event.getId());
            }
        }
    }

    private static void updateCalendarInCalendarList(@NotNull Calendar service) throws IOException {
        // Retrieve the calendar list entry
        CalendarListEntry calendarListEntry = service.calendarList().get("c_classroom65013eff@group.calendar.google.com").execute();

        // Make a change of the color
        calendarListEntry.setBackgroundColor("#10cad4");

        // Update the altered entry
        CalendarListEntry updatedCalendarListEntry =
                service.calendarList().update(calendarListEntry.getId(), calendarListEntry).setColorRgbFormat(true).execute();

        System.out.println(updatedCalendarListEntry.getBackgroundColor());
    }

    public static void iterateOverCalendersInCalendarList(@NotNull Calendar service) throws IOException {
        String pageToken = null;
        do {
            CalendarList calendarList = service.calendarList().list().setPageToken(pageToken).execute();
            List<CalendarListEntry> items = calendarList.getItems();

            for (CalendarListEntry calendarListEntry : items) {
                System.out.println(calendarListEntry.getSummary() + "   and calendar id: " + calendarListEntry.getId());
            }
            pageToken = calendarList.getNextPageToken();
        } while (pageToken != null);

    }

    public static void createNewCalendarInCalendarList(@NotNull Calendar service) throws IOException {

        // Create a new calendar list entry
        CalendarListEntry calendarListEntry = new CalendarListEntry();
        calendarListEntry.setId("test5649687346calendar");

        // Insert the new calendar list entry
        CalendarListEntry createdCalendarListEntry = service.calendarList().insert(calendarListEntry).execute();

        System.out.println(createdCalendarListEntry.getSummary() + "and it's id: " + createdCalendarListEntry.getId());
    }

    public static void deleteCalendarInCalendarList(@NotNull Calendar service,String calendarId) throws IOException {

        // Delete calendar from calendar list entry
        String response = String.valueOf(service.calendarList().delete(calendarId).execute());
        System.out.println(response);
        if (response.equals("null")){
            System.out.print("Deleted successfully...");
        }


    }
}
