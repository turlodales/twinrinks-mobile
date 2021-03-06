package com.gigaStorm.twinRinks;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.CalendarContract;

/**
 * <code>Data_CalendarManager</code> handles the addition of events to a user's
 * calendar.
 * 
 * @author Boris Dubinsky
 * @author Andrew Mass
 */
@SuppressLint("NewApi")
public class Data_CalendarManager {

  private Context context;

  private Data_MemoryManager memoryManager;

  private Util util;

  public Data_CalendarManager(Context context) {
    this.context = context;
    memoryManager = new Data_MemoryManager(context);
    util = new Util(context);
  }

  public void saveGamesToCalendar() {
    showCalendarPopup();
  }

  private void showCalendarPopup() {
    final ContentResolver cr;
    final Cursor result;
    final Uri uri;
    List<String> listCals = new ArrayList<String>();
    final String[] projection = new String[] {BaseColumns._ID,
        CalendarContract.Calendars.ACCOUNT_NAME,
        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
        CalendarContract.Calendars.NAME,};

    uri = CalendarContract.Calendars.CONTENT_URI;

    cr = context.getContentResolver();
    result = cr.query(uri, projection, null, null, null);

    if(result.getCount() > 0 && result.moveToFirst()) {
      do {
        listCals.add(result.getString(result
            .getColumnIndex(CalendarContract.Calendars.NAME)));
      }
      while(result.moveToNext());
    }

    CharSequence[] calendars = listCals.toArray(new CharSequence[listCals
        .size()]);

    AlertDialog.Builder builder = new AlertDialog.Builder(context);
    builder.setTitle("Calendar to use:");
    builder.setItems(calendars, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int itemCal) {
        loopThroughGames(itemCal);
      };
    });

    AlertDialog alert = builder.create();
    alert.show();
  }

  private void loopThroughGames(int whichCalendar) {
    ArrayList<Model_Game> games = memoryManager.getGames();

    for(Model_Game g: games) {
      for(Model_Team t: memoryManager.getUserTeams()) {
        if((g.getTeamA().equalsIgnoreCase(t.getTeamName()) || g.getTeamH()
            .equalsIgnoreCase(t.getTeamName()))
            && g.getLeague().equalsIgnoreCase(t.getLeague()) && !g.hasPassed()) {
          addGameToCalendar(g, whichCalendar + 1);
        }
      }
    }
  }

  private void addGameToCalendar(Model_Game game, int whichCalendar) {
    ContentResolver cr = context.getContentResolver();
    ContentValues values = new ContentValues();

    try {
      values.put(CalendarContract.Events.CALENDAR_ID, whichCalendar);
      values.put(CalendarContract.Events.TITLE, "Hockey- " + game.getLeague()
          + ": " + game.getTeamH() + " vs " + game.getTeamA());
      values.put(CalendarContract.Events.EVENT_LOCATION,
          "Twin Rinks Ice Arena - " + game.getRink() + " Rink");
      values.put(CalendarContract.Events.DTSTART, game.getCal()
          .getTimeInMillis());
      values.put(CalendarContract.Events.DTEND,
          game.getCal().getTimeInMillis() + 5400000);
      values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault()
          .getID());
      cr.insert(CalendarContract.Events.CONTENT_URI, values);
    }
    catch(Exception e) {
      util.err(e.getMessage());
    }
  }
}
