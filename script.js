const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

exports.sendAnimalAlertNotification = functions.database
    .ref("/detected_animals/{userId}")
    .onWrite((change, context) => {
        const afterData = change.after.val();
        if (!afterData) return null; 
        

        const payload = {
            notification: {
                title: "⚠️ Animal Alert",
                body: `${afterData.label} detected at ${afterData.timestamp}`,
            },
            topic: "alerts"
        };

        return admin.messaging().send(payload)
            .then(() => console.log("Notification sent successfully"))
            .catch(err => console.error("Error sending notification:", err));
    });
