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
 */

public class RelationshipManager {
    /*package private*/ RelationshipManager() {

    }

    public void searchUser(String search, final AccountManager.Account.QueryListener listener) {
        ParseQuery usernameQuery = ParseUser.getQuery();
        usernameQuery.whereEqualTo(AccountManager.FIELD_USERNAME, search);

        ParseQuery phoneQuery = ParseUser.getQuery();
        phoneQuery.whereEqualTo(AccountManager.FIELD_PHONE_NUMBER, search);

        ArrayList<ParseQuery<ParseObject>> queries = new ArrayList<>();
        queries.add(usernameQuery);
        queries.add(phoneQuery);

        ParseQuery compoundQuery = ParseQuery.or(queries);
        compoundQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (listener != null) {
                    if (e == null) {
                        ArrayList<User> users = new ArrayList<User>();
                        for (ParseObject o : objects) {
                            if (o.getObjectId() != ParseUser.getCurrentUser().getObjectId()) {
                                users.add(new User(o.getString(AccountManager.FIELD_USERNAME_CASE), o.getString(AccountManager.FIELD_DISPLAY_NAME), o.getString(AccountManager.FIELD_PHONE_NUMBER)));
                            }
                        }

                        listener.onSearchUser(users);
                    } else {

                    }
                }
            }
        });
    }

    public void getRelationships(final AccountManager.Account.QueryListener listener) {
        ParseQuery fromQuery = ParseQuery.getQuery("Relationships");
        fromQuery.whereEqualTo("from", ParseUser.getCurrentUser());

        ParseQuery toQuery = ParseQuery.getQuery("Relationships");
        toQuery.whereEqualTo("to", ParseUser.getCurrentUser());

        ArrayList<ParseQuery<ParseObject>> queries = new ArrayList<>();
        queries.add(fromQuery);
        queries.add(toQuery);

        ParseQuery compoundQuery = ParseQuery.or(queries);
        compoundQuery.include("from");
        compoundQuery.include("to");

        compoundQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (listener != null) {
                    ArrayList<Relationship> relationships = new ArrayList<Relationship>();

                    for (ParseObject o : objects) {
                        ParseUser userRel   = null;
                        boolean   fromMe  = true;
                        if (!((ParseUser)o.get("from")).getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                            userRel = (ParseUser)o.get("from");
                            fromMe  = false;
                        } else {
                            userRel = (ParseUser)o.get("to");
                        }

                        Log.d("Relationship: ", userRel.getUsername());

                        String displayName = userRel.get("displayName").toString();
                        if (displayName == null) displayName = "";
                        Relationship rel = new Relationship(new User(userRel.get(AccountManager.FIELD_USERNAME_CASE).toString(), displayName, userRel.get(AccountManager.FIELD_PHONE_NUMBER).toString()), fromMe, userRel.getInt("status"));
                        relationships.add(rel);
                    }

                    listener.onGotRelationships(relationships);
                }
            }
        });
    }

    public void requestFriend(String requestType, String reqString) {
        ParseQuery query = ParseUser.getQuery();
        query.whereEqualTo(requestType, reqString);
        query.setLimit(1);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(final List<ParseUser> foundUsers, ParseException e) {
                if (e != null) {

                } else {
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
                                    if ((int)objects.get(0).get("status") == Relationship.STATUS_PENDING) {
                                        objects.get(0).put("status", Relationship.STATUS_ACCEPTED);
                                        objects.get(0).saveInBackground();

                                        push.put("message", ParseUser.getCurrentUser().getUsername() + " has accepted your friend request!");
                                    }
                                } else {
                                    //Found user - so add him
                                    ParseObject pRelationship = new ParseObject("Relationships");
                                    pRelationship.put("from", ParseUser.getCurrentUser());
                                    pRelationship.put("to", foundUsers.get(0));
                                    pRelationship.put("status", Relationship.STATUS_PENDING);

                                    pRelationship.saveInBackground();

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
}
