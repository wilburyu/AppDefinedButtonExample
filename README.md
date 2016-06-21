# AppDefinedButtonExample

Instead of using navigation buttons such as up/down/left/right, Reticle Remote framework offers functionalities for an Android application to define its own button set in the Reticle Remote app.

Libraries & Dependencies:
a). com.osterhoutgroup.api.ext.jar
b). buttons.xml

There are three types of button set:
1). Video button type:
  Once an application reports it is video related and programmatically calls our API, launchVideoPlayerController(). Reticle Remote app will pop up the video controller.

2). Music button type:
  Same as video button type. Once an app calls our API, setMusicPlayerState(), with specific parameters, Reticle Remote app will pop up a music controller.

3). App Defined Button Set:
  Instead of using the aforementioned pre-defined button set, developers can customize a button set for their own application. 
  Steps:
  1). define your own buttons.xml and save it in assets folder. For details on how to create the xml file, please look at the attached sample file inside the assets folder.
  2). In the AndroidManifest.xml, add the following meta data to your activity.
            <meta-data
                android:name="display_defined_buttons"
                android:value="buttons.xml" />
  3). Compile and run your app.

This sample app shows how to creat an application with app defined buttons.
