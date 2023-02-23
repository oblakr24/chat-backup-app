## Chat Backup: Privacy policy

This is an open-source application published under the Apache 2.0 license.

The source code is available in a public repository: https://github.com/oblakr24/chat-backup-app

The app does not communicate with any external services, and although contact and SMS info data is collected, the user has full control over what to do with it.
The app generates an export file using the SMS and contact data with the temporary file stored in the app's private storage. 

A system's sharing picker is then used to expose and share this file to external apps, which is fully controlled by the user.

### App permissions and their usage

As per the `AndroidManifest.xml` file, the following permissions are used:

https://github.com/oblakr24/chat-backup-app/blob/main/app/src/main/AndroidManifest.xml

<br/>

| Permission | Reason                                                                                                                       |
| :---: |------------------------------------------------------------------------------------------------------------------------------|
| `android.permission.READ_SMS` | Reading of SMS messages to display them in the app and export them                                                           |
| `android.permission.READ_CONTACTS` | Reading of contacts to resolve the display names of the displayed numbers, and to export them in the file.                   |
| `android.permission.RECEIVE_SMS` | Receiving of SMS messages.                                                                                                   |
| `android.permission.SEND_SMS` | Sending SMS messages.                                                                                                        |
| `android.permission.RECEIVE_MMS` | Receiving MMS messages. |

 <hr style="border:1px solid gray">

Note the absence of a permission to access the internet, as this app makes no communication with any external service.

Functionalities listed in the permissions section require the application to be set as the default SMS messaging application.
For this reason, a system prompt is displayed upon interacting with any such action. The user is then presented with the choice to pick the desired default SMS messaging application.