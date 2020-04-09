# FoodApp

We made it boys! 


This is an android application that serves a food based social media platform used for sharing pictures, recipes, and other related content. This version of the app only utilizes pictures, however in the future it would be ideal to have video based content and all the other stuff the cool social media sites are doing these days.

# Functionality

Just create an account and login/explore lol. You'll figure out how it works.

### Backend

The backend is supported by Firebase thanks to the very nice folks at Google. There are three libraries written, each to support content storage (Firebase Storage), data storage (Cloud Firestore), and authorization (Firebase Authorization).  In the future, Firebase’s cloud base storage could be used to scale with ease and expand to IOS and Desktop users with minimal maintenance. 

### User Authorization

User authorization is handled by Firebase but the users are managed manually. In order to sign up, users need to provide a name, username, date of birth, email, and password. Before creating an account, all users will have to verify their email by following instructions sent to them via email. The same verification process must be followed when resetting a password.  Implementation of this process was done using Firebase Dynamic Links, so there were limitations applied to how the verification steps could be executed and tested.

### Data

There are two sample users that provide the content displayed when logged in. Since there aren’t many users, the data retrieval is fairly trivial, but this can be improved upon when more data is collected. The majority of the data was scraped from Food.com, but has been modified to fit the purposes of this app.

### Further Implementation
Features to be implemented at a later date

- Recipe importation
- Image Uploads
- Video Recording
- Edit Profile/ Add Stuff there
- Sharing content
- Everything in Discover


Sheesh... I think its finally done. The struggle is real boys!
