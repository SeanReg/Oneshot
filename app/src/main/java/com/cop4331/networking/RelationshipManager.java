package com.cop4331.networking;

import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Sean on 11/26/2016.
 *
 * Class to manage an Accounts relationships
 */
public class RelationshipManager {
    /**
     * Instantiates a new Relationship manager.
     */
/*package private*/ RelationshipManager() {

    }

    /**
     * Searches for a user by their username or phone number
     * @param search   the String to search by
     * @param listener the listener to notify with results
     */
    public void searchUser(String search, final AccountManager.Account.QueryListener listener) {
        //Username query
        ParseQuery usernameQuery = ParseUser.getQuery();
        usernameQuery.whereEqualTo(AccountManager.FIELD_USERNAME, search);

        //Phone number query
        ParseQuery phoneQuery = ParseUser.getQuery();
        phoneQuery.whereEqualTo(AccountManager.FIELD_PHONE_NUMBER, search);

        ArrayList<ParseQuery<ParseObject>> queries = new ArrayList<>();
        queries.add(usernameQuery);
        queries.add(phoneQuery);

        //Perform query
        ParseQuery compoundQuery = ParseQuery.or(queries);
        compoundQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (listener != null) {
                    if (e == null) {

                        //Results
                        ArrayList<User> users = new ArrayList<User>();
                        for (ParseObject o : objects) {
                            if (o.getObjectId() != ParseUser.getCurrentUser().getObjectId()) {
                                //Create User from ParseObject
                                users.add(new User(o.getString(AccountManager.FIELD_USERNAME_CASE), o.getString(AccountManager.FIELD_DISPLAY_NAME), o.getString(AccountManager.FIELD_PHONE_NUMBER), (ParseUser)o));
                            }
                        }

                        listener.onSearchUser(users);
                    } else {

                    }
                }
            }
        });
    }

    /**
     * Gets the relationships for the current Account
     * @param listener the listener to notify with tresults
     */
    public void getRelationships(final AccountManager.Account.QueryListener listener) {
        //Construct from query
        ParseQuery fromQuery = ParseQuery.getQuery("Relationships");
        fromQuery.whereEqualTo("from", ParseUser.getCurrentUser());

        //Construct to query
        ParseQuery toQuery = ParseQuery.getQuery("Relationships");
        toQuery.whereEqualTo("to", ParseUser.getCurrentUser());

        //Compound query
        ArrayList<ParseQuery<ParseObject>> queries = new ArrayList<>();
        queries.add(fromQuery);
        queries.add(toQuery);

        ParseQuery compoundQuery = ParseQuery.or(queries);
        compoundQuery.include("from");
        compoundQuery.include("to");

        //Perform query
        compoundQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (listener != null) {
                    ArrayList<Relationship> relationships = new ArrayList<Relationship>();

                    //Get list of relationships
                    for (ParseObject o : objects) {
                        ParseUser userRel   = null;
                        boolean   fromMe  = true;

                        //Find the sender
                        if (!((ParseUser)o.get("from")).getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                            userRel = (ParseUser)o.get("from");
                            fromMe  = false;
                        } else {
                            userRel = (ParseUser)o.get("to");
                        }

                        Log.d("Relationship: ", userRel.getUsername());

                        //Construct relationship objects
                        String displayName = userRel.get("displayName").toString();
                        if (displayName == null) displayName = "";

                        Relationship rel = new Relationship(new User(userRel.get(AccountManager.FIELD_USERNAME_CASE).toString(), displayName, userRel.get(AccountManager.FIELD_PHONE_NUMBER).toString(), userRel), fromMe, o.getInt("status"));

                        //Add to list
                        relationships.add(rel);
                    }

                    //Notify listener
                    listener.onGotRelationships(relationships);
                }
            }
        });
    }

    /**
     * Sends a friend request to the specified user
     * @param requestType the field to match the reqString with
     * @param reqString   the String to match
     */
    public void requestFriend(String requestType, String reqString) {
        //Search query by requestType
        ParseQuery query = ParseUser.getQuery();
        query.whereEqualTo(requestType, reqString);
        query.setLimit(1);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(final List<ParseUser> foundUsers, ParseException e) {
                if (e != null) {

                } else {
                    //Ensure the current user is different
                    if (foundUsers.get(0).equals(ParseUser.getCurrentUser())) return;

                    //Lets first see if a request for us already exists
                    ParseQuery rQuery = ParseQuery.getQuery("Relationships");
                    rQuery.whereEqualTo("to", ParseUser.getCurrentUser());
                    rQuery.whereEqualTo("from", foundUsers.get(0));
                    rQuery.setLimit(1);
                    rQuery.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> objects, ParseException e) {
                            if (e != null) {

                            } else {
                                HashMap<String, String> push = new HashMap<String, String>();
                                push.put("userId", foundUsers.get(0).getObjectId());

                                if (objects.size() > 0) {
                                    //Already have a relationship with the User
                                    if ((int)objects.get(0).getInt("status") == Relationship.STATUS_PENDING) {
                                        //Auto accept friend requests
                                        objects.get(0).put("status", Relationship.STATUS_ACCEPTED);
                                        objects.get(0).saveInBackground();

                                        //Push notification
                                        push.put("message", ParseUser.getCurrentUser().getUsername() + " has accepted your friend request!");
                                    }
                                } else {
                                    //Found user - so add him
                                    ParseObject pRelationship = new ParseObject("Relationships");
                                    pRelationship.put("from", ParseUser.getCurrentUser());
                                    pRelationship.put("to", foundUsers.get(0));
                                    pRelationship.put("status", Relationship.STATUS_PENDING);

                                    pRelationship.saveInBackground();

                                    //Push notification
                                    push.put("message", ParseUser.getCurrentUser().getUsername() + " has sent you a friend request!");
                                }

                                ParseCloud.callFunctionInBackground("PushUser", push);
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * Removes a relationship from the current Account
     * @param relationship the relationship to remove
     */
    public void removeFriend(Relationship relationship) {
        //Query from to
        ParseQuery rQuery = ParseQuery.getQuery("Relationships");
        rQuery.whereEqualTo("to", ParseUser.getCurrentUser());
        rQuery.whereEqualTo("from", relationship.getUser().getParseUser());

        //Query to from
        ParseQuery sQuery = ParseQuery.getQuery("Relationships");
        sQuery.whereEqualTo("to", relationship.getUser().getParseUser());
        sQuery.whereEqualTo("from", ParseUser.getCurrentUser());

        //Compound query
        ArrayList<ParseQuery<ParseObject>> queries = new ArrayList<>();
        queries.add(rQuery);
        queries.add(sQuery);

        ParseQuery compoundQuery = ParseQuery.or(queries);
        compoundQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null && objects.size() > 0) {
                    //Perform delete
                    objects.get(0).deleteInBackground();
                }
            }
        });
    }

}
