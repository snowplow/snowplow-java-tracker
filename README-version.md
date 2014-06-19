/**
 * JavaPlow com.snowplowanalytics.snowplow.tracker.Tracker

    Instructions to Use:

     Instantiate a com.snowplowanalytics.snowplow.tracker.PayloadMap and a com.snowplowanalytics.snowplow.tracker.Tracker:
      com.snowplowanalytics.snowplow.tracker.PayloadMap pd = new com.snowplowanalytics.snowplow.tracker.PayloadMapC();
      com.snowplowanalytics.snowplow.tracker.Tracker t1 = new com.snowplowanalytics.snowplow.tracker.TrackerC("collector_uri","namespace");

     Configure payload if needed:
      pd.add_json("{'Movie':'Shawshank Redemption', 'Time':'100 Minutes' }");

     Attach the payload to the com.snowplowanalytics.snowplow.tracker.Tracker:
      t1.setPayload(pd);

     Configure the payload as you must:
      t1.setUserID("Kevin");
      t1.setLanguage("eng");
      t1.setPlatform("cnsl");
      t1.setScreenResolution(1260, 1080);
      t1.track();

     Call the track function when configured:
      t1.track()

    A HttpGet request is configured based on the parameters passed in
     and sent to the collector URI to be stored in a database.

    Current supported platforms include "pc", "tv", "mob", "cnsl", and "iot".

    Current Tracking commands:
     t1.track()
       Submits the current payload to CloudFront server

     track_page_view(page_url, page_title, referrer, context)
       All strings, null context or empty string allowed.
       Configures the URI further adding page data to payload

    Notes:
     Dictionary and JSON context values should be in String format or Map<String,Object> e.g. "{'name':'Kevin', ...}"
     I would recommend using one tracker for one tracking instance type.
       This is because only certain fields are refreshed every loop to reduce overhead at high iteration speed.
     Arrays, Dictionaries, JSON contest is all homogeneous, must all be of the String type.

*/

