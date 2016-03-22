# Introduction
ScratchAPI is a simple Java interface to the Scratch 2.0 website. It is not nearly done yet, and will later include several features, but it takes time. :P


# Documentation
### Session Management
Create a user session and log in:
```
ScratchSession session = Scratch.createSession("username", "password");
```

Register a new user (not working as of now):
```
ScratchSession session = Scratch.register("username", "password", "gender", birthMonth, "birthYear", "location", "the@email.here"); // All fields case sensitive-- to be documented better later...
```

Get information about the session:
```
String username = session.getUsername();
String csrftoken = session.getCSRFToken();
String expiration = session.getExpiration();
String sessionid = session.getSessionID();
ScratchCloudSession cloudSession = session.getCloudSession(projectid); // Cloud sessions are documented a while down
ScratchUser user = session.you();
```

Log session out:
```
session.logout();
```

------

### Users
Create a user instance:
```
ScratchUser user = new ScratchUser("username");
```

Get information about user:
```
String username = user.getUsername();
int messageCount = user.getMessageCount();
List<ScratchProject> favoriteProjects = user.getFavoriteProjects(limit, offset); // limit max 20
```

Follow and unfollow the user:
```
user.setFollowing(session, true); // Follow user
user.setFollowing(session, false); // Unfollow user
```

Comment on user profile:
```
user.comment(session, "Example comment"); // You can't comment too fast, remember the delay
```

TODO:
```
/* TODO: 
 * - Get user description
 * - Get what I am working on
 * - Get location
 * - Get status (Scratcher, New Scratcher, Team Member, etc) 
 * - Get project listing
 * - Get loved project and their count, and favorite project's count
 * - Get followers and following list
 * - Get comments on user's profile in a List<ScratchComment>?
 * - Get user manager as a ScratchUserManager?
 */
```

------

### Projects
Create a project instance:
```
ScratchProject project = new ScratchProject(projectid); // 'projectid' is an int
```

Get information about project:
```
project.update(); // Run this before everything else
String title = project.getTitle();
String description = project.getDescription();
ScratchUser creator = project.getCreator();
int viewCount = project.getViewCount();
int loveCount = project.getLoveCount();
int favoriteCount = project.getFavoriteCount();
int projectid = project.getProjectID();
ScratchProjectManager manager = project.getProjectManager(); // Useless as of now
String resourceurl = project.getResourceURL();
String shareDate = project.getShareDate();
String thumbnailurl = project.getThumbnailURL();
```

Love and unlove the project:
```
project.setLoved(session, true); // Love project
project.setLoved(session, false); // Unlove project
```

Favorite and unfavorite the project:
```
project.setFavorited(session, true); // Favorite project
project.setFavorited(session, false); // Unfavorite project
```

Comment on project:
```
project.comment(session, "Example comment"); // You also can't comment too fast, remember the delay
```

------

### Cloud Data Management
Create a cloud session:
```
ScratchCloudSession cloudSession = session.getCloudSession(projectid);
```

Get the cloud symbol (☁):
```
char cloudSymbol = Scratch.CLOUD;
```

Get information about cloud session:
```
int projectid = cloudSession.getProjectID();
ScratchSession session = cloudSession.getScratchSession();
String cloudToken = cloudSession.getCloudToken();
```

Get a cloud variable's contents:
```
String contents = cloudSession.get(Scratch.CLOUD + " variable"); // The space is needed!
```

Set a cloud variable's contents **(not working)**:
```
cloudSession.set(Scratch.CLOUD + " variable", "new value");
```

Rename a cloud variable **(untested)**:
```
cloudSession.rename(Scratch.CLOUD + " variable", Scratch.CLOUD + " newName");
```

Create a cloud variable **(untested)**:
```
cloudSession.create(Scratch.CLOUD + " newVariable", "value");
```

Delete a cloud variable **(untested)**:
```
cloudSession.delete(Scratch.CLOUD + " variable");
```

Close the cloud session:
```
cloudSession.close();
```

------

### Statistics
Get total project count:
```
int totalProjectCount = ScratchStatistics.getProjectCount(); // Updates every 24hrs (thanks @thisandagain)
```

TODO:
```
/* TODO:
 * - Like everything... :P
 */
```

------

### Miscellaneous
Get a list of Scratch users:
```
List<ScratchUser> users = Scratch.getUsers(limit, offset); // Max limit is 20
```

TODO:
```
/* TODO:
 * - Get list of featured projects
 * - Get list of newest projects
 * - Get list of curated projects
 * - Get list of team members (?)
 * - Get list of top-remixed projects
 * - Get list of top-loved projects
 * - Get list of featured studios
 * - Get current project curator
 * - Get Scratch design studio
 * - Get list of projects by following
 * - Get list of projects by following's loved projects
 * - Get list of projects in studios I'm following
 */
```

------

### Exceptions
List of exceptions:
```
edu.mit.scratch.exceptions.ScratchException
edu.mit.scratch.exceptions.ScratchLoginException
edu.mit.scratch.exceptions.ScratchUserException
edu.mit.scratch.exceptions.ScratchProjectException
edu.mit.scratch.exceptions.ScratchStatisticalException
```
