Site Stats Tool
--------------------------
1. About
2. Building/deploying
3. Using the tool
4. Known bugs/issues
5. Contact



1. About
-----------------------
Site Stats is a tool for Sakai for showing site statistics by user, event, or resource.
The tool’s main page shows a weekly snapshot of activity in the site and simple statistics.

For the Sakai 2.1.x releases, use Site Stats Tool 0.2.x versions.
For the Sakai 2.2.x releases, use Site Stats Tool 0.3.x versions.

Currently, it has an overview page with summary information about site visits and activity,
an events page listing site events per user, a resources page listing site resources accessed
by user, and a preferences page.

This tool is in development and is not yet optimized. Optimization will be done after gathering
users feedback. Feel free to send suggestions to ufpuv-suporte@ufp.pt or by using the
SiteStats JIRA section.


2. Building / Deploying
-----------------------
The standard way in Sakai 2.x is to copy the source folder into the Sakai
source tree and run maven on it ('maven sakai').
Alternatively, you can place this tool source folder in other folder as long
as you link '../master' to the 'master' folder of the Sakai source tree (Sakai
uses a master project descriptor file at '../master/project.xml').


3. Using the Tool
-----------------------
Use the 'Site Info' tool to add SiteStats to a site.
In the events and resources page, events can searched by user ID or email, filtered by groups,
or by time period.
The preferences page allows a user to remove or add events.
 

4. Known bugs/issues/limitations
-----------------------
Currently, it queries SAKAI_SESSION and SAKAI_EVENT once per tool page access in a user
session and keeps data in memory. Data will only be updated in the following cases:
    *  user logs in;
    * reseting the tool in the title bar;
    * changing the tool preferences;
    * searching in Events and Resources pages (updates these pages only);
Optimization will be implemented in a near future.

As of Sakai 2.1.2, the Assignments tool has two bugs related with event logging: grading or
releasing grades logs "asn.submit.submission" (the same event that is logged when an user
performs an assignment submission).


5. Contact
-----------------------
SiteStats is written by Nuno Fernandes at Universidade Fernando Pessoa.
If you wish, feel free to submit patches or any other contributions.
You may contact us at ufpuv-suporte@ufp.pt and nuno@ufp.pt.
