# Introduction
ScratchAPI is a simple Java interface to the Scratch 2.0 website. It is not nearly done yet, and will later include several features, but it takes time. :P

If you want to see a list of in-progress features, click here > https://github.com/Arinerron/ScratchAPI/issues/1

[![Build Status](https://travis-ci.com/Arinerron/ScratchAPI.svg?token=xRJQhWcuhJai95gtzHzi&branch=master)](https://travis-ci.com/Arinerron/ScratchAPI) [![Gitter](https://badges.gitter.im/Arinerron/ScratchAPI.svg)](https://gitter.im/Arinerron/ScratchAPI?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)


# Documentation
### Jump to Section
- [Session Management](#session-management)
- [Users](#users)
- [Projects](#projects)
- [Cloud Data Management](#cloud)
- [Statistics](#statistics)
- [Miscellaneous](#misc)
- [Exceptions](#exceptions)

------

### Session Management<a name="session-management"></a>
Create a user session and log in:
```java
ScratchSession session = Scratch.createSession("username", "password");
```

Register a new user (not working as of now):
```java
ScratchSession session = Scratch.register("username", "password", "gender", birthMonth, "birthYear", "location", "the@email.here"); // All fields case sensitive-- to be documented better later...
```

Get information about the session:
```java
String username = session.getUsername();
String csrftoken = session.getCSRFToken();
String expiration = session.getExpiration();
String sessionid = session.getSessionID();
ScratchCloudSession cloudSession = session.getCloudSession(projectid); // Cloud sessions are documented a while down
ScratchUser user = session.you();
```

Log session out:
```java
session.logout();
```

------

### Users<a name="users"></a>
Create a user instance:
```java
ScratchUser user = new ScratchUser("username");
```

Get information about user:
```java
String username = user.getUsername();
int messageCount = user.getMessageCount();
List<ScratchProject> favoriteProjects = user.getFavoriteProjects(limit, offset); // limit max 20
```

Follow and unfollow the user:
```java
user.setFollowing(session, true); // Follow user
user.setFollowing(session, false); // Unfollow user
```

Comment on user profile:
```java
user.comment(session, "Example comment"); // You can't comment too fast, remember the delay
```

TODO:
```java
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

### Projects<a name="projects"></a>
Create a project instance:
```java
ScratchProject project = new ScratchProject(projectid); // 'projectid' is an int
```

Get information about project:
```java
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
```java
project.setLoved(session, true); // Love project
project.setLoved(session, false); // Unlove project
```

Favorite and unfavorite the project:
```java
project.setFavorited(session, true); // Favorite project
project.setFavorited(session, false); // Unfavorite project
```

Comment on project:
```java
project.comment(session, "Example comment"); // You also can't comment too fast, remember the delay
```

------

### Cloud Data Management<a name="cloud"></a>
Create a cloud session:
```java
ScratchCloudSession cloudSession = session.getCloudSession(projectid);
```

Get the cloud symbol (☁):
```java
char cloudSymbol = Scratch.CLOUD;
```

Get information about cloud session:
```java
int projectid = cloudSession.getProjectID();
ScratchSession session = cloudSession.getScratchSession();
String cloudToken = cloudSession.getCloudToken();
```

Get a cloud variable's contents:
```java
String contents = cloudSession.get(Scratch.CLOUD + " variable"); // The space is needed!
```

Set a cloud variable's contents **(not working)**:
```java
cloudSession.set(Scratch.CLOUD + " variable", "new value");
```

Rename a cloud variable **(untested)**:
```java
cloudSession.rename(Scratch.CLOUD + " variable", Scratch.CLOUD + " newName");
```

Create a cloud variable **(untested)**:
```java
cloudSession.create(Scratch.CLOUD + " newVariable", "value");
```

Delete a cloud variable **(untested)**:
```java
cloudSession.delete(Scratch.CLOUD + " variable");
```

Close the cloud session:
```java
cloudSession.close();
```

------

### Statistics<a name="statistics"></a>
Get total project count:
```java
int totalProjectCount = ScratchStatistics.getProjectCount(); // Updates every 24hrs (thanks @thisandagain)
```

TODO:
```java
/* TODO:
 * - Like everything... :P
 */
```

------

### Miscellaneous<a name="misc"></a>
Get a list of Scratch users:
```java
List<ScratchUser> users = Scratch.getUsers(limit, offset); // Max limit is 20
```

TODO:
```java
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

### Exceptions<a name="exceptions"></a>
List of exceptions:
```java
edu.mit.scratch.exceptions.ScratchException
edu.mit.scratch.exceptions.ScratchLoginException
edu.mit.scratch.exceptions.ScratchUserException
edu.mit.scratch.exceptions.ScratchProjectException
edu.mit.scratch.exceptions.ScratchStatisticalException
```


# Examples
### Follow yourself
```java
new ScratchUser(session.getUsername()).setFollowing(session, true);
```

------

### Open alert dialog when you get a message
```java
final String user = "griffpatch"; // Change this to your username
		
Timer timer = new Timer();
timer.schedule(new TimerTask() {
	public ScratchUser target = new ScratchUser(user);
	public int previous = -1;
	
	@Override
	public void run() {
		try {
			int count = target.getMessageCount();
			if(previous == -1) {
				previous = count; // This means the program just started.
			} else 
				if(count > previous) {
					previous = count;
					javax.swing.JOptionPane.showMessageDialog(null, "The account " + user + " on Scratch now has " + count + " messages.");
				}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}, 0, 5000);
```

**Warning:** if you are famous, the number of dialogs from your messages will seriously get annoying.
