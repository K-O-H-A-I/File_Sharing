const passport = require('passport');
const GoogleStrategy = require('passport-google-oauth20').Strategy;
const User = require('../models/User');  // Import your User model

// Serialize user to store user ID in the session
passport.serializeUser((user, done) => {
    done(null, user.id);  // Storing user ID in session
});

// Deserialize user to retrieve the user from the ID stored in session
passport.deserializeUser(async (id, done) => {
    try {
        const user = await User.findById(id);  // Find the user by their ID
        done(null, user);  // Attach user object to request
    } catch (err) {
        done(err, null);  // Handle errors
    }
});

// Google OAuth Strategy configuration
passport.use(new GoogleStrategy({
    clientID: process.env.GOOGLE_CLIENT_ID,  // Your Google client ID
    clientSecret: process.env.GOOGLE_CLIENT_SECRET,  // Your Google client secret
    callbackURL: '/auth/google/callback'  // URL to which Google will redirect after authentication
},
async (accessToken, refreshToken, profile, done) => {
    try {
        // Check if user already exists in database
        let user = await User.findOne({ googleId: profile.id });

        if (user) {
            return done(null, user);  // If user exists, return the user
        } else {
            // If user doesn't exist, create a new user
            user = new User({
                googleId: profile.id,
                displayName: profile.displayName,
                email: profile.emails[0].value,
                // You can add more profile fields if needed
            });

            await user.save();  // Save the new user
            return done(null, user);  // Return the new user
        }
    } catch (err) {
        return done(err, null);  // Handle errors
    }
}));
