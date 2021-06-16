# example of deploying chatclient-cb.jar

# this is the script carnageblender.com (an OpenACS 3.2.5 site)
# uses to start the chatclient-cb applet.  Note that I've
# had to create a "de-packaged" jar from the four source
# files; this is necessary b/c IE's default JRE bites.
# If you don't care about supporting unupgraded IE installs
# then by all means feel free to use chatclient-cb.jar
# unmodified.

# Note that it's best to set the applet to 100% size, and
# let this script's containing frame control the size.
# This lets the user resize painlessly.

set user_id [ad_maybe_redirect_for_registration]

# ChatBlender uses spaces as command delimiters so escape those
# (On this site no other whitespace is allowed so we can ignore tabs)
db_1row name_q "select replace(last_name, ' ', '_') as name, password from users where user_id = $user_id"

ns_return 200 text/html "
[ad_header Chat]
    <applet code=Client.class archive=Client-nopackage.jar width=100% height=100%>
    <param name=port value=7777>
    <param name=user value=\"$name\">
    <param name=password value=\"$password\">
    <param name=channel value=carnage>
    </applet>
</body>
</html>
"
