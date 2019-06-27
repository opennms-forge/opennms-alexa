# OpenNMS Alexa Skill 

This is a proof-of-concept implementation of an OpenNMS Alexa Skill. The following stuff works so far:

* Alexa, show nodes
  * in the results list you can pick a node for details, e.g. "Alexa, pick two"
* Alexa, show outages
  * in the results list you can pick an outage for details, e.g. "Alexa, pick two"
* Alexa, show alarms
  * in the results list you can pick an alarm for details, e.g. "Alexa, pick two"
* Alexa, any alarms
* Alexa, any outages
* Alexa, help

## Building and deploying

The project's repository can be cloned and deployed by the following commands:

    git clone git@github.com:opennms-forge/opennms-alexa.git
    cd opennms-alexa
    mvn wildfly:run
    
In order for the endpoint to be accepted by Amazon, it must be protected by SSL on the default port (443). This isn't documented anywhere. It took me days to figure out, that only the standard HTTPS port can be used.

## Requirements

* you need a free Amazon developer account
* you need a SSL-enabled server using the standard HTTPS (443) port
* you need a running OpenNMS instance
* you need a Echo device with a screen

## Amazon developer account

After creating your account [https://developer.amazon.com](https://developer.amazon.com) create your new skill.

![Cat](https://github.com/opennms-forge/opennms-alexa/raw/master/docs/screenshot1.png)

Now you can define the skill's invocation command. Please note that the term `open` cannot be part of the name. So, we choose `opehn n. m. s.` here.

![Cat](https://github.com/opennms-forge/opennms-alexa/raw/master/docs/screenshot2.png)

At this point we define the utterances to use for the model. Choose the language at top and copy/paste the JSON utterance definitions from the repository in the JSON Editor. Hit `Save Model` and `Build Model` when you are done.

![Cat](https://github.com/opennms-forge/opennms-alexa/raw/master/docs/screenshot3.png)

We need to define the endpoint URLs where the servlet is deployed.

![Cat](https://github.com/opennms-forge/opennms-alexa/raw/master/docs/screenshot4.png)

Finally, you can use the Test tab to test your skill. Just select `Development` for the option `Skill testing is enabled in`.

![Cat](https://github.com/opennms-forge/opennms-alexa/raw/master/docs/screenshot5.png)

You can now use the skill with your Echo devices:

![Cat](https://github.com/opennms-forge/opennms-alexa/raw/master/docs/echo-device.png)

