---
layout: page
title: Using Kaa endpoint SDKs
permalink: /:path/
nav: /:path/Programming-guide/Using-Kaa-endpoint-SDKs
sort_idx: 40
---
Using Kaa endpoint SDKs


* [Introduction](#introduction)

## Introduction

Developing some IoT solution we all the time facing the same routine - creating network communication stack, log delivery functionality, event exchange between endpoints and etcetera. All of this functionality already provides Kaa platform and you can get it out of the box using Kaa endpoint SDK.

An endpoint SDK is a library which provides communication, data marshalling, persistence, and other functions available in Kaa for specific type of an endpoint (e.g. [Java-based](Using-Kaa-Java-endpoint-SDK), [Android-based](), [C++-based](Using-Kaa-Cpp-endpoint-SDK), [C-based](Using-Kaa-C-endpoint-SDK), [Objective-C-based](Using-Kaa-Objective-C-endpoint-SDK)). The SDK is designed to be embedded into your devices and managed applications, while Kaa cluster constitutes the middleware "cloud" basis for a specific solution. The SDK works in conjunction with the cluster. It is the responsibility of the Kaa client to process structured data provided by the Kaa server (configuration, notifications, etc.) and to supply data to the return path interfaces (profiles, logs, etc.).

Endpoint SDK helps to save time on development routine and allows to concentrate on your business logic.

For more detailed overview refer to [Design reference]().

## Configuration Kaa SDK

* [Basic architecture](#basic-architecture)
* [Configuring Kaa](#configuring-kaa)
* [Working with Client-side endpoint profiles](#working-with-client-side-endpoint-profiles)
* [Working with Server-side endpoint profiles](#working-with-server-side-endpoint-profiles)

Endpoint profile is a virtual identity or "passport" of the endpoint. By filtering against the data in the profiles, endpoints may be aggregated into independently managed groups. Please review the [Kaa profiling design reference]() for more background.


This guide will familiarize you with the basic concepts of designing endpoint profiles and programming the Kaa profiling subsystem. It is assumed that you have either set up a [Kaa Sandbox](), or a [full-blown Kaa cluster]() already and that you have created a [tenant]() and an [application]() in Kaa.


### Basic architecture

The following diagram illustrates basic entities and data flows in scope of the endpoint profile management:

* Endpoint profiles are generated based on the client-side and server-side endpoint profile schemas and corresponding profile  data. 
* Endpoints send their client-side profiles to the Kaa server during the registration.
* Server-side applications and scripts may use Kaa REST API in order to modify server-side profile of endpoints.
* The Kaa tenant developer specifies profile filters for endpoint groups using either Admin UI or REST API
* Kaa Operations service classifies endpoints into groups based on their profiles and the group profile filters

![](images/basic_architecture.png)

### Configuring Kaa

The default client-side profile schema installed for Kaa applications is empty. In order to make use of the Kaa profiling / identity management capabilities, you should load at least either client-side or server-side profile schema that reflects the application you are designing. Please note that client-side structure is used during SDK generation and changes to the client-side structure requires generation of new SDK. However, Application developer is able to define and change server-side structure of endpoint profile at any time.

### Working with Client-side endpoint profiles

Think about the client-side profile schema as of a structured data set of your endpoint application that will later be available to you in Kaa server and may change due to your client application logic or device state.
You can configure your own client-side profile schema using the Admin UI or REST API. For the purpose of this guide we will use a fairly abstract client-side profile schema shown below.

```json{
    "type":"record",
    "name":"Profile",
    "namespace":"org.kaaproject.kaa.schema.sample.profile",
    "fields":[
        {
            "name":"id",
            "type":"string"
        },
        {
            "name":"os",
            "type":{
                "type":"enum",
                "name":"OS",
                "symbols":[
                    "Android",
                    "iOS",
                    "Linux"
                ]
            }
        },
        {
            "name":"os_version",
            "type":"string"
        },
        {
            "name":"build",
            "type":"string"
        }
    ]
}
```

Client-side endpoint profile updates are reported to the endpoint SDK using a profile container. The profile related API varies depending on the target SDK platform, however the general approach is the same.

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Java">Java</a></li>
  <li><a data-toggle="tab" href="#C_plus_plus">C++</a></li>
  <li><a data-toggle="tab" href="#C">C</a></li>
  <li><a data-toggle="tab" href="#Objective-C">Objective-C</a></li>
</ul>

<div class="tab-content">
<div id="Java" class="tab-pane fade in active" markdown="1" >

```java
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.profile.ProfileContainer;
import org.kaaproject.kaa.client.DesktopKaaPlatformContext;
import org.kaaproject.kaa.schema.sample.profile.OS;
import org.kaaproject.kaa.schema.sample.profile.Profile;
 
// Profile is an auto-generated class based on user defined schema.
Profile profile;
// Desktop Kaa client initialization
public void init() {
    // Create instance of desktop Kaa client for Kaa SDK
    KaaClient client = Kaa.newClient(new DesktopKaaPlatformContext(),
            new SimpleKaaClientStateListener());
    // Sample profile
    profile = new Profile("id", OS.Linux, "3.17", "0.0.1-SNAPSHOT");
    // Simple implementation of ProfileContainer interface that is provided by the SDK
    client.setProfileContainer(new ProfileContainer() {
        @Override
        public Profile getProfile() {
            return profile;
        }
    });
    // Starts Kaa
    client.start();
    // Update to profile variable
    profile.setBuild("0.0.1-SNAPSHOT");
    // Report update to Kaa SDK. Force delivery of updated profile to server.
    client.updateProfile();
}
```

</div>

<div id="C_plus_plus" class="tab-pane fade" markdown="1" >

```c++
#include <memory>
 
#include <kaa/Kaa.hpp>
#include <kaa\profile\DefaultProfileContainer.hpp>
 
using namespace kaa;
 
...
 
// Create an endpoint instance
auto kaaClient = Kaa::newClient();
// Create an endpoint's profile
Profile profile;
profile.id = "deviceId";
profile.os_version = "3.17";
profile.os = OS::Linux;
profile.build = "0.0.1-SNAPSHOT";
 
// Set a profile container to pass a profile to an endpoint
kaaClient->setProfileContainer(std::make_shared<DefaultProfileContainer>(profile));
 
// Call each time when a profile is updated
kaaClient->updateProfile();
 
// Start an endpoint
kaaClient->start();
```

</div>

<div id="C" class="tab-pane fade" markdown="1" >

```c
#include <kaa/kaa_profile.h>
#include <kaa/gen/kaa_profile_gen.h> // auto-generated header
 
#define KAA_EXAMPLE_PROFILE_ID "sampleid"
#define KAA_EXAMPLE_OS_VERSION "1.0"
#define KAA_EXAMPLE_BUILD_INFO "3cbaf67e"
 
kaa_client_t *kaa_client = /* ... */;
 
/* Assume Kaa SDK is already initialized */
 
/* Create and update profile */
kaa_profile_t *profile = kaa_profile_profile_create();
 
profile->id = kaa_string_move_create(KAA_EXAMPLE_PROFILE_ID, NULL);
profile->os = ENUM_OS_Linux;
profile->os_version = kaa_string_move_create(KAA_EXAMPLE_OS_VERSION, NULL);
profile->build = kaa_string_move_create(KAA_EXAMPLE_BUILD_INFO, NULL);
 
kaa_error_t error_code = kaa_profile_manager_update_profile(kaa_client_get_context(kaa_client)->profile_manager, profile);
 
/* Check error code */
 
profile->destroy(profile);

```
</div>

<div id="Objective-C" class="tab-pane fade" markdown="1" >

```objective-c
#import <Kaa/Kaa.h>
#import "ViewController.h"
 
@interface ViewController () <ProfileContainer>
 
@property (nonatomic, strong) KAAProfile *profile;
 
@end
 
@implementation ViewController
- (void)initClient() {
    // Create instance of Kaa client
    id<KaaClient> client = [Kaa client];
    // Sample profile that is an auto-generated class based on user defined schema.
    _profile = [[KAAProfile alloc] initWithId:@"id" os:OS_LINUX code:@"3.17" build:@"0.0.1-SNAPSHOT"];
    // Simple implementation of ProfileContainer interface that is provided by the SDK
    [client setProfileContainer:self];
    // Starts Kaa
    [client start];
    // Update to profile variable
    _profile.build = @"0.0.1-SNAPSHOT";
    // Report update to Kaa SDK. Force delivery of updated profile to server.
    [client updateProfile];
}
 
- (KAAProfile *)getProfile {
    return _profile;
}
 
...
 
@end

```
</div>
</div>

### Working with Server-side endpoint profiles

Think about the server-side profile schema as of a set of your endpoint properties the are controlled by your server-side applications. For example, client subscription plan, device activation flag, etc. You may also use server-side endpoint profile to store properties that are set during manufacturing and should not be controlled by client application.


You can configure your own server-side profile schema using the [Admin UI]() or [REST API](). For the purpose of this guide we will use a fairly abstract server-side profile schema shown below.

```json
{
    "type":"record",
    "name":"ServerProfile",
    "namespace":"org.kaaproject.kaa.schema.sample.profile",
    "fields":[
        {
            "name":"subscriptionPlan",
            "type":"string"
        },
        {
            "name":"activationFlag",
            "type":"boolean"
        }
    ]
}
```

Once this schema is configured, you are able to assign server-side endpoint profile body to certain endpoints based on their ids using [Admin UI]() or [REST API]().

## Messaging across endpoints

* [Basic architecture](#basic-architecture-1)
* [Configuring Kaa](#configuring-kaa-1)
  * [Creating ECFs](#creating-ecfs)
  * [Application mapping](#application-mapping)
  * [Generating SDK](#generating-sdk)
* [Coding](#coding)
  * [Attach endpoint to user](#attach-endpoint-to-user)
  * [Assisted attach](#assisted-attach)
  * [Get ECF factory and create ECF object](#get-ecf-factory-and-create-ecf-object)
    * [Get ECF factory from Kaa](#get-ecf-factory-from-kaa)
    * [Get specific ECF object from ECF factory](#get-specific-ecf-object-from-ecf-factory)
  * [Send events](#send-events)
    * [Get endpoint addresses](#get-endpoint-addresses)
    * [Send one event to all endpoints](#send-one-event-to-all-endpoints)
    * [Send one event to one endpoint](#send-one-event-to-one-endpoint)
    * [Send batch of events to endpoint(s)](#send-batch-of-events-to-endpoints)
  * [Receive events](#receive-events)

The Kaa event subsystem enables messages delivery across the endpoints (EP). Events can be thought of as commands or structured chunks of data. For example, an event from a smartphone application can toggle the lights in the room.


Structure of the data carried by events is defined by an Event Class (EC) data schema configured on the Kaa server and built into Kaa SDK. ECs are grouped into Event Class Families (ECF) by the topic. Kaa allows delivering events among EPs that belong to the same or different applications. As of Kaa r0.6, endpoints must be associated with the same user in order to be able to send events to each other. Please review the [events system design reference]() for more background.


From this guide you will learn how to use Kaa events functionality for enabling communication across the endpoints.

### Basic architecture
The following diagram illustrates basic entities and data flows in scope of the event management:

* Events are generated based on the [event class schema]() created by the developer for the [event class family]()
* The event class family can be used by one or multiple applications, thus the event can be shared between applications
* Several applications belonging to the same user share events between each other  according to the [application mapping]() and through the Kaa server

![](images/basic_architecture_cluster_endpoint.png)

### Configuring Kaa

This section provides guidance on how to configure ECFs in Kaa.

#### Creating ECFs

ECFs are shared between applications and there is no default ECFs created automatically. A tenant admin can create multiple ECFs on the Kaa server using the Admin UI or REST API.
In this guide, we will use the following ECF that allows you to remotely control a thermostat using your cell phone.

```json
[ 
    { 
        "namespace":"org.kaaproject.kaa.schema.sample.thermo",
        "type":"record",
        "classType":"event",
        "name":"ThermostatInfoRequest",
        "fields":[ 
 
        ]
    },
    { 
        "namespace":"org.kaaproject.kaa.schema.sample.thermo",
        "type":"record",
        "classType":"object",
        "name":"ThermostatInfo",
        "fields":[ 
            { 
                "name":"currentTemperature",
                "type":"int"
            },
            { 
                "name":"targetTemperature",
                "type":"int"
            }
        ]
    },
    { 
        "namespace":"org.kaaproject.kaa.schema.sample.thermo",
        "type":"record",
        "classType":"event",
        "name":"ThermostatInfoResponse",
        "fields":[ 
            { 
                "name":"thermostatInfo",
                "type":"org.kaaproject.kaa.schema.sample.thermo.ThermostatInfo"
            }
        ]
    },
    { 
        "namespace":"org.kaaproject.kaa.schema.sample.thermo",
        "type":"record",
        "classType":"event",
        "name":"ChangeTemperatureCommand",
        "fields":[ 
            { 
                "name":"temperature",
                "type":"int"
            }
        ]
    }
]
```

#### Application mapping

Our sample ECF contains three events: ThermostatInfoRequest, ThermostatInfoResponse and ChangeTemperatureCommand. We will create the following two applications in this guide: Controller and Thermostat. It is logical that Controller should be able to send the ThermostatInfoRequest event (act as a source) and receive the ThermostatInfoResponse event (act as a sink), while Thermostat should be able to send the ThermostatInfoResponse event and receive the ThermostatInfoRequest event. Based on such logic of our application, we will create the following application mapping for our sample ECF:

| Application | ThermostatInfoRequest | ThermostatInfoResponse | ChangeTemperatureCommand |
|-------------|-----------------------|------------------------|--------------------------|
| Controller  | source                | sink                   | source                   |
| Thermostat  | sink                  | source                 | sink                     |



A tenant admin can set up application mapping using the [Admin UI]() or [REST API]().

#### Generating SDK

During the SDK generation, the Control server generates the event object model and extends APIs to support methods for sending events and registering event listeners. An application SDK can support multiple ECFs. However, it cannot simultaneously support multiple versions of the same ECF.

### Coding
This section provides code samples which illustrate practical usage of events in Kaa. The event subsystem API varies depending on the target SDK platform, but the general approach is the same.

#### Attach endpoint to user


To enable sending/receiving events to/from endpoints, at first the client should attach the endpoint to the user as shown in the following screenshot.

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Java-1">Java</a></li>
  <li><a data-toggle="tab" href="#C_plus_plus-1">C++</a></li>
  <li><a data-toggle="tab" href="#C-1">C</a></li>
  <li><a data-toggle="tab" href="#Objective-C-1">Objective-C</a></li>
</ul>

<div class="tab-content">
<div id="Java-1" class="tab-pane fade in active" markdown="1" >

```java
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.KaaDesktop;
import org.kaaproject.kaa.client.event.registration.UserAuthResultListener;
 
kaaClient.attachUser("userExternalId", "userAccessToken", new UserAttachCallback()
{
    @Override
    public void onAttachResult(UserAttachResponse response) {
        System.out.println("Attach response" + response.getResult());
    }
});
```

</div>
<div id="C_plus_plus-1" class="tab-pane fade" markdown="1" >

```c++
#include <memory>
#include <iostream>
 
#include <kaa/Kaa.hpp>
#include <kaa/event/registration/SimpleUserAttachCallback.hpp>
 
using namespace kaa;
 
class SimpleUserAttachCallback : public IUserAttachCallback {
public:
    virtual void onAttachSuccess()
    {
        std::cout << "Endpoint is attached to a user" << std::endl;
    }
 
    virtual void onAttachFailed(UserAttachErrorCode errorCode, const std::string& reason)
    {
        std::cout << "Failed to attach endpoint to a user: error code " << errorCode << ", reason '" << reason << "'" << std::endl;
    }
};
 
...
  
// Create an endpoint instance
auto kaaClient = Kaa::newClient();
 
// Start an endpoint
kaaClient->start();
 
// Try to attach an endpoint to a user
kaaClient->attachUser("userExternalId", "userAccessToken", std::make_shared<SimpleUserAttachCallback>());
```

</div>
<div id="C-1" class="tab-pane fade" markdown="1" >

```c
#include <kaa/kaa_user.h>
#include <kaa/platform/ext_user_callback.h>
 
kaa_client_t *kaa_client = /* ... */;
 
kaa_error_t on_attached(void *context, const char *user_external_id, const char *endpoint_access_token)
{
    return KAA_ERR_NONE;
}
kaa_error_t on_detached(void *context, const char *endpoint_access_token)
{
    return KAA_ERR_NONE;
}
kaa_error_t on_attach_success(void *context)
{
    return KAA_ERR_NONE;
}
kaa_error_t on_attach_failed(void *context, user_verifier_error_code_t error_code, const char *reason)
{
    return KAA_ERR_NONE;
}
kaa_attachment_status_listeners_t attachement_listeners = 
{
        NULL,
        &on_attached,
        &on_detached,
        &on_attach_success,
        &on_attach_failed
};
/* Assume Kaa SDK is already initialized */
kaa_error_t error_code = kaa_user_manager_set_attachment_listeners(kaa_client_get_context(kaa_client)->user_manager
                                                                 , &attachement_listeners);
/* Check error code */
error_code = kaa_user_manager_default_attach_to_user(kaa_client_get_context(kaa_client)->user_manager
                                                   , "userExternalId"
                                                   , "userAccessToken");
/* Check error code */
```

</div>
<div id="Objective-C-1" class="tab-pane fade" markdown="1" >

```objective-c
#import <Kaa/Kaa.h>
 
@interface ViewController () <UserAttachDelegate>
 
...
 
- (void)prepareUserForMessaging {
    [self.kaaClient attachUserWithId:@"userExternalId" accessToken:@"userAccessToken" delegate:self];
}
- (void)onAttachResult:(UserAttachResponse *)response {
    NSLog(@"Attach response: %i", response.result);
}
```

</div>
</div>


#### Assisted attach

Specific endpoint may not be able to attach itself independently. E.g. in case if endpoint doesn't have an user token. Another endpoint that already attached can assist in attachment process of the new endpoint. Below are examples of assisted attachment.

**C:**

```c
#include <stdint.h>
#include <kaa/kaa_error.h>
#include <kaa/platform/kaa_client.h>
#include <kaa/kaa_user.h>
#include <kaa/platform/ext_user_callback.h>
 
kaa_client_t *kaa_client = /* ... */;
 
kaa_attachment_status_listeners_t assisted_attachment_listeners =  /* ... */;
 
/* Assume Kaa SDK is already initialized */
 
kaa_error_t error_code = kaa_user_manager_default_attach_to_user(kaa_client_get_context(kaa_client)->user_manager
                                                                , "userExternalId"
                                                                , "userAccessToken");
 
/* Check error code */
 
error_code = kaa_user_manager_attach_endpoint(kaa_client_get_context(kaa_client)->user_manager, "externalAccessToken", &assisted_attachment_listeners);
 
/* Check error code */
```

### Get ECF factory and create ECF object
To access the Kaa event functionality, the client should implement the two following blocks of code.

#### Get ECF factory from Kaa

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Java-2">Java</a></li>
  <li><a data-toggle="tab" href="#C_plus_plus-2">C++</a></li>
  <li><a data-toggle="tab" href="#Objective-c-2">Objective-C</a></li>
</ul>

<div class="tab-content">
<div id="Java-2" class="tab-pane fade in active" markdown="1" >

```java
import org.kaaproject.kaa.client.event.EventFamilyFactory;
 
EventFamilyFactory eventFamilyFactory = kaaClient.getEventFamilyFactory();
```

</div><div id="C_plus_plus-2" class="tab-pane fade" markdown="1" >

```c++
#include <kaa/event/gen/EventFamilyFactory.hpp>
 
...
EventFamilyFactory& eventFamilyFactory = kaaClient->getEventFamilyFactory();
```

</div><div id="Objective-c-2" class="tab-pane fade" markdown="1" >

```objective-c
#import <Kaa/Kaa.h>
 
...
 
EventFamilyFactory *eventFamilyFactory = [kaaClient getEventFamilyFactory];
```

</div></div>

#### Get specific ECF object from ECF factory

<ul class="nav nav-tabs">
<li class="active"><a data-toggle="tab" href="#Java-3">Java</a></li>
  <li><a data-toggle="tab" href="#C_plus_plus-3">C++</a></li>
  <li><a data-toggle="tab" href="#Objective-c-3">Objective-C</a></li>
</ul>

<div class="tab-content">
<div id="Java-3" class="tab-pane fade in active" markdown="1" >

```java
import org.kaaproject.kaa.demo.smarthouse.thermo.ThermoEventClassFamily;
 
ThermoEventClassFamily tecf = eventFamilyFactory.getThermoEventClassFamily();
```

</div><div id="C_plus_plus-3" class="tab-pane fade" markdown="1" >

```c++
#include <kaa/event/gen/ThermoEventClassFamily.hpp>
 
...
ThermoEventClassFamily& tecf = eventFamilyFactory.getThermoEventClassFamily();
```

</div><div id="Objective-c-3" class="tab-pane fade" markdown="1" >

```objective-c
#import<Kaa/Kaa.h>
 
...
 
ThermostatEventClassFamily *tecf = [self.eventFamilyFactory getThermostatEventClassFamily];
```

</div></div>

### Send events
To send one or more events, the client should proceed as described in this section.

#### Get endpoint addresses
Execute the asynchronous findEventListeners method to request a list of the endpoints supporting all specified EC FQNs (FQN stands for fully qualified name).

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Java-4">Java</a></li>
  <li><a data-toggle="tab" href="#C_plus_plus-4">C++</a></li>
  <li><a data-toggle="tab" href="#C-4">C</a></li>
  <li><a data-toggle="tab" href="#Objective-C-4">Objective-C</a></li>
</ul>

<div class="tab-content">
<div id="Java-4" class="tab-pane fade in active" markdown="1" >

```java
import org.kaaproject.kaa.client.event.FindEventListenersCallback;
 
List<String> FQNs = new LinkedList<>();
FQNs.add(ThermostatInfoRequest.class.getName());
FQNs.add(ChangeTemperatureCommand.class.getName());
 
kaaClient.findEventListeners(FQNs, new FindEventListenersCallback() {
    @Override
    public void onEventListenersReceived(List<String> eventListeners) {
        // Some code
    }   
    @Override
    public void onRequestFailed() {
        // Some code
    }
});
```

</div><div id="C_plus_plus-4" class="tab-pane fade" markdown="1" >

```c++
#include <list>
#include <memory>
#include <string>
#include <vector>
 
#include <kaa/event/IFetchEventListeners.hpp>
 
class SimpleFetchEventListeners : public IFetchEventListeners {
public:
    virtual void onEventListenersReceived(const std::vector<std::string>& eventListeners)
    {
        // Some code
    }
 
    virtual void onRequestFailed()
    {
        // Some code
    }
};
 
...
std::list<std::string> FQNs = {"org.kaaproject.kaa.schema.sample.thermo.ThermostatInfoRequest"
                              ,"org.kaaproject.kaa.schema.sample.thermo.ChangeTemperatureCommand"};
 
kaaClient->findEventListeners(FQNs, std::make_shared<SimpleFetchEventListeners>());
```

</div><div id="C-4" class="tab-pane fade" markdown="1" >

```c
#include <kaa/event.h>
#include <kaa/platform/ext_event_listeners_callback.h>
 
const char *fqns[] = { "org.kaaproject.kaa.schema.sample.thermo.ThermostatInfoRequest"
                     , "org.kaaproject.kaa.schema.sample.thermo.ChangeTemperatureCommand" };
 
kaa_error_t event_listeners_callback(void *context, const kaa_endpoint_id listeners[], size_t listeners_count)
{
    /* Process response */
    return KAA_ERR_NONE;
}
 
kaa_error_t event_listeners_request_failed(void *context)
{
    /* Process failure */
    return KAA_ERR_NONE;
}
 
kaa_event_listeners_callback_t callback = { NULL
                                          , &event_listeners_callback
                                          , &event_listeners_request_failed };
 
kaa_error_t error_code = kaa_event_manager_find_event_listeners(kaa_client_get_context(kaa_client)->event_manager
                                                              , fqns
                                                              , 2
                                                              , &callback);
 
/* Check error code */
```

</div><div id="Objective-C-4" class="tab-pane fade" markdown="1" >

```objective-c
#import <Kaa/Kaa.h>
 
@interface ViewController () <FindEventListenersDelegate>
 
...
 
    NSArray *listenerFQNs = @[[ThermostatInfoRequest FQN], [ChangeDegreeRequest FQN]];
    [self.kaaClient findListenersForEventFQNs:listenerFQNs delegate:self];
 
- (void)onEventListenersReceived:(NSArray *)eventListeners {
    // Some code
}
 
- (void)onRequestFailed {
    // Some code
}
```

</div></div>

#### Send one event to all endpoints
To send an event to all endpoints which were previously located by the findEventListeners method, execute the sendEventToAll method upon the specific ECF object.

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Java-5">Java</a></li>
  <li><a data-toggle="tab" href="#C_plus_plus-5">C++</a></li>
  <li><a data-toggle="tab" href="#C-5">C</a></li>
  <li><a data-toggle="tab" href="#Objective-C-5">Objective-C</a></li>
</ul>

<div class="tab-content">
<div id="Java-5" class="tab-pane fade in active" markdown="1" >

```java
import org.kaaproject.kaa.schema.sample.thermo.ThermostatInfoRequest;
 
tecf.sendEventToAll(new ThermostatInfoRequest());
```

</div><div id="C_plus_plus-5" class="tab-pane fade" markdown="1" >

```c++
#include <kaa/event/gen/ThermoEventClassFamilyGen.hpp>
 
...
nsThermoEventClassFamily::ThermostatInfoRequest thermoRequest;
tecf.sendEventToAll(thermoRequest);
```

</div><div id="C-5" class="tab-pane fade" markdown="1" >

```c
#include <kaa/gen/kaa_thermo_event_class_family.h>
 
/* Create and send an event */
kaa_thermo_event_class_family_thermostat_info_request_t* thermo_request = kaa_thermo_event_class_family_thermostat_info_request_create();
 
kaa_error_t error_code = kaa_event_manager_send_kaa_thermo_event_class_family_thermostat_info_request(kaa_client_get_context(kaa_client)->event_manager
                                                                                                    , thermo_request
                                                                                                    , NULL);
 
/* Check error code */
 
thermo_request->destroy(thermo_request);
```

</div><div id="Objective-C-5" class="tab-pane fade" markdown="1" >

```objective-c
#import<Kaa/Kaa.h>
 
...
 
KAAUnion *degree = [KAAUnion unionWithBranch:KAA_UNION_INT_OR_NULL_BRANCH_0 data:@(-30)];
ChangeDegreeRequest *changeDegree = [[ChangeDegreeRequest alloc] initWithDegree:degree];
 
// Assume the target variable is one of the received in the findEventListeners method
[tecf sendChangeDegreeRequest:changeDegree to:target];
```

</div></div>

#### Send one event to one endpoint
To send an event to a single endpoint which was previously located by the findEventListeners method, execute the sendEvent method upon the specific ECF object and this endpoint.

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Java-6">Java</a></li>
  <li><a data-toggle="tab" href="#C_plus_plus-6">C++</a></li>
  <li><a data-toggle="tab" href="#C-6">C</a></li>
  <li><a data-toggle="tab" href="#Objective-C-6">Objective-C</a></li>
</ul>

<div class="tab-content">
<div id="Java-6" class="tab-pane fade in active" markdown="1" >

```java
import org.kaaproject.kaa.schema.sample.thermo.ChangeTemperatureCommand;
 
ChangeTemperatureCommand ctc = new ChangeTemperatureCommand(-30);
// Assume the target variable is one of the received in the findEventListeners method
tecf.sendEvent(ctc, target);
```

</div><div id="C_plus_plus-6" class="tab-pane fade" markdown="1" >

```c++
#include <kaa/event/gen/ThermoEventClassFamilyGen.hpp>
 
...
nsThermoEventClassFamily::ChangeTemperatureCommand ctc;
ctc.temperature = -30;
 
// Assume the target variable is one of the received in the findEventListeners method
tecf.sendEvent(ctc, target);
```

</div><div id="C-6" class="tab-pane fade" markdown="1" >

```c
#include <kaa/geb/kaa_thermo_event_class_family.h>
 
/* Create and send an event */
kaa_endpoint_id target_endpoint;
kaa_thermo_event_class_family_change_temperature_command_t* change_command = kaa_thermo_event_class_family_change_temperature_command_create();
change_command->temperature = -30;
 
kaa_error_t error_code = kaa_event_manager_send_kaa_thermo_event_class_family_change_temperature_command(kaa_client_get_context(kaa_client)->event_manager
                                                                                                       , change_command
                                                                                                       , target_endpoint);
/* Check error code */
 
change_command->destroy(change_command);
```

</div><div id="Objective-C-6" class="tab-pane fade" markdown="1" >

```objective-c
#import<Kaa/Kaa.h>
 
...
 
KAAUnion *degree = [KAAUnion unionWithBranch:KAA_UNION_INT_OR_NULL_BRANCH_0 data:@(-30)];
ChangeDegreeRequest *changeDegree = [[ChangeDegreeRequest alloc] initWithDegree:degree];
 
// Assume the target variable is one of the received in the findEventListeners method
[tecf sendChangeDegreeRequest:changeDegree to:target];
```

</div></div>

#### Send batch of events to endpoint(s)
To send a batch of events at once to a single or all endpoints, execute the following code.

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Java-7">Java</a></li>
  <li><a data-toggle="tab" href="#C_plus_plus-7">C++</a></li>
  <li><a data-toggle="tab" href="#C-7">C</a></li>
  <li><a data-toggle="tab" href="#Objective-C-7">Objective-C</a></li>
</ul>

<div class="tab-content">
<div id="Java-7" class="tab-pane fade in active" markdown="1" >

```java
import org.kaaproject.kaa.client.event.EventFamilyFactory;
import org.kaaproject.kaa.demo.smarthouse.thermo.ThermoEventClassFamily;
import org.kaaproject.kaa.schema.sample.thermo.ThermostatInfoRequest;
import org.kaaproject.kaa.schema.sample.thermo.ChangeTemperatureCommand;
 
// Get instance of EventFamilyFactory
EventFamilyFactory eventFamilyFactory = kaaClient.getEventFamilyFactory();
ThermoEventClassFamily tecf = eventFamilyFactory.getThermoEventClassFamily();
 
// Register a new event block and get a unique block id
TransactionId trxId = eventFamilyFactory.startEventsBlock();
 
// Add events to the block
// Adding a broadcasted event to the block
tecf.addEventToBlock(trxId, new ThermostatInfoRequest());
// Adding a targeted event to the block
tecf.addEventToBlock(trxId, new ChangeTemperatureCommand(-30), "home_thermostat");
 
 
// Send an event batch
eventFamilyFactory.submitEventsBlock(trxId);
// Or cancel an event batch
eventFamilyFactory.removeEventsBlock(trxId);
```

</div><div id="C_plus_plus-7" class="tab-pane fade" markdown="1" >

```c++
#include <kaa/event/gen/EventFamilyFactory.hpp>
#include <kaa/event/gen/ThermoEventClassFamily.hpp>
#include <kaa/event/gen/ThermoEventClassFamilyGen.hpp>
 
using namespace kaa;
 
// Get an instance of EventFamilyFactory
EventFamilyFactory& eventFamilyFactory = kaaClient->getEventFamilyFactory();
ThermoEventClassFamily& tecf = eventFamilyFactory.getThermoEventClassFamily();
 
// Register a new event block and get a unique block id
TransactionIdPtr trxId = eventFamilyFactory.startEventsBlock();
 
// Add events to the block
// Adding a broadcasted event to the block
nsThermoEventClassFamily::ThermostatInfoRequest thermoRequest;
tecf.addEventToBlock(trxId, thermoRequest);
// Adding a targeted event to the block
nsThermoEventClassFamily::ChangeTemperatureCommand ctc;
ctc.temperature = -30;
tecf.addEventToBlock(trxId, ctc, "home_thermostat");
 
 
// Send an event batch
eventFamilyFactory.submitEventsBlock(trxId);
 
// Or cancel an event batch
eventFamilyFactory.removeEventsBlock(trxId); 
```

</div><div id="C-7" class="tab-pane fade" markdown="1" >

```c
#include <kaa/kaa_event.h>
#include <kaa/gen/kaa_thermo_event_class_family.h>
 
kaa_event_block_id transaction_id;
 
kaa_error_t error_code = kaa_event_create_transaction(kaa_context->event_manager, &transaction_id);
/* Check error code */
 
kaa_thermo_event_class_family_thermostat_info_request_t* thermo_request = kaa_thermo_event_class_family_thermostat_info_request_create();
kaa_thermo_event_class_family_change_temperature_command_t* change_command = kaa_thermo_event_class_family_change_temperature_command_create();
change_command->temperature = 5;
 
error_code = kaa_event_manager_add_kaa_thermo_event_class_family_thermostat_info_request_event_to_block(kaa_client_get_context(kaa_client)->event_manager
                                                                                                      , thermo_request
                                                                                                      , NULL
                                                                                                      , transaction_id);
/* Check error code */
 
kaa_endpoint_id target_endpoint;
error_code = kaa_event_manager_add_kaa_thermo_event_class_family_change_temperature_command_event_to_block(kaa_client_get_context(kaa_client)->event_manager
                                                                                                         , change_command
                                                                                                         , target_endpoint
                                                                                                         , transaction_id);
/* Check error code */
 
error_code = kaa_event_finish_transaction(kaa_client_get_context(kaa_client)->event_manager, transaction_id);
/* Check error code */
 
thermo_request->destroy(thermo_request);
change_command->destroy(change_command);
```

</div><div id="Objective-C-7" class="tab-pane fade" markdown="1" >

```objective-c
#import <Kaa/Kaa.h>
 
...
// Get instance of EventFamilyFactory
EventFamilyFactory *eventFamilyFactory = [kaaClient getEventFamilyFactory];
ThermostatEventClassFamily *tecf = [eventFamilyFactory getThermostatEventClassFamily];
 
// Register a new event block and get a unique block id
TransactionId *trxId = [eventFamilyFactory startEventsBlock];
 
// Add events to the block
// Adding a broadcasted event to the block
[tecf addThermostatInfoRequestToBlock:[[ThermostatInfoRequest alloc] init] withTransactionId:trxId];
// Adding a targeted event to the block
ChangeDegreeRequest *request = [[ChangeDegreeRequest alloc] init];
request.degree = [KAAUnion unionWithBranch:KAA_UNION_INT_OR_NULL_BRANCH_0 data:@(-30)];
[tecf addChangeDegreeRequestToBlock:request withTransactionId:trxId target:@"home_thermostat"];
 
 
// Send an event batch
[eventFamilyFactory submitEventsBlockWithTransactionId:trxId];
// Or cancel an event batch
[eventFamilyFactory removeEventsBlock:trxId];
```

</div></div>

### Receive events
To start listening to incoming events, execute the addListener method upon the specific ECF object.

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Java-8">Java</a></li>
  <li><a data-toggle="tab" href="#C_plus_plus-8">C++</a></li>
  <li><a data-toggle="tab" href="#C-8">C</a></li>
  <li><a data-toggle="tab" href="#Objective-C-8">Objective-C</a></li>
</ul>

<div class="tab-content">
<div id="Java-8" class="tab-pane fade in active" markdown="1" >

```java
import org.kaaproject.kaa.demo.smarthouse.thermo.ThermoEventClassFamily;
 
tecf.addListener(new ThermoEventClassFamily.Listener() {
    @Override
    public void onEvent(ChangeTemperatureCommand event, String source) {
        // Some code
    }
    @Override
    public void onEvent(ThermostatInfoResponse event, String source) {
        // Some code
    }
    @Override
    public void onEvent(ThermostatInfoRequest event, String source) {
        // Some code
    }
});
```

</div><div id="C_plus_plus-8" class="tab-pane fade" markdown="1" >

```c++
#include <kaa/event/gen/ThermoEventClassFamilyGen.hpp>
 
class SimpleThermoEventClassFamilyListener: public ThermoEventClassFamily::ThermoEventClassFamilyListener {
public:
    virtual void onEvent(const nsThermoEventClassFamily :: ThermostatInfoRequest& event, const std::string& source) 
    {
        // Some code
    }
    virtual void onEvent(const nsThermoEventClassFamily :: ThermostatInfoResponse& event, const std::string& source) 
    {
        // Some code
    }
    virtual void onEvent(const nsThermoEventClassFamily :: ChangeTemperatureCommand& event, const std::string& source) 
    {
        // Some code
    }
};
...
SimpleThermoEventClassFamilyListener eventsListener;
tecf.addEventFamilyListener(eventsListener);
```

</div><div id="C-8" class="tab-pane fade" markdown="1" >

```c
#include <kaa/kaa_event.h>
#include <kaa/gen/kaa_thermo_event_class_family.h>
 
void on_thermo_event_class_family_change_temperature_command(void *context, kaa_thermo_event_class_family_change_temperature_command_t *event, kaa_endpoint_id_p source)
{
    /* Process event */
    event->destroy(event);
}
kaa_error_t error_code = kaa_event_manager_set_kaa_thermo_event_class_family_change_temperature_command_listener(kaa_client_get_context(kaa_client)->event_manager
                                                                                                               , &on_thermo_event_class_family_change_temperature_command
                                                                                                               , NULL);
/* Check error code */
```

</div><div id="Objective-C-8" class="tab-pane fade" markdown="1" >

```objective-c
#import <Kaa/Kaa.h>
 
@interface ViewController () <ThermostatEventClassFamilyDelegate>
 
...
 
[self.tecf addDelegate:self];
- (void)onThermostatInfoRequest:(ThermostatInfoRequest *)event fromSource:(NSString *)source {
    // Some code
}
 
- (void)onThermostatInfoResponse:(ThermostatInfoResponse *)event fromSource:(NSString *)source {
    // Some code
}
 
- (void)onChangeDegreeRequest:(ChangeDegreeRequest *)event fromSource:(NSString *)source {
    // Some code
}
```

</div></div>

* [Basic architecture](#basic-architecture-2)
* [Configuring Kaa](#configuring-kaa-2)
  * [Notification topics](#notification-topics)
  * [Sending notifications](#sending-notifications)
* [Coding](#coding)
  * [Get available topics](#get-available-topics)
  * [Subscribe to updates on available topics](#subscribe-to-updates-on-available-topics)
  * [Default notification listener](#default-notification-listener)
  * [Topic specific notification listener](#topic-specific-notification-listener)
  * [Subscribe to optional topics](#subscribe-to-optional-topics)

The Kaa notification subsystem enables delivery of messages from the Kaa server to endpoints. The structure of the data that is carried by notifications is defined by the notification schema, which is configured on the Kaa server and built into Kaa endpoints. Please review the Kaa [notifications design reference]() for more details.

This guide will familiarize you with the basic concepts of Kaa notifications and programming of the Kaa notification subsystem. It is assumed that you have already set up either a [Kaa Sandbox]() or a [full-blown Kaa cluster]() and that you have created at least one [tenant]() and one [application]() in Kaa. We also recommend that you review [collecting endpoint profiles guide]() and [using endpoint groups]() before you proceed with this guide.

### Basic architecture
The following diagram illustrates basic entities and data flows in scope of the notification management:
Notifications are generated based on the notification schema created by the developer for the application 
The user or admin sends a notification using either Admin UI or REST API

The following diagram illustrates basic entities and data flows in scope of the notification management:
* Notifications are generated based on the [notification schema]() created by the developer for the application 
* The user or admin sends a notification using either [Admin UI]() or [REST API]()

![](images/basic_architecture_notification.png)

### Configuring Kaa
This section provides guidance on how to configure notifications in Kaa.

**Notification schema**


The default notification schema installed for Kaa applications is empty. You can configure your own notification schema using the Admin UI or REST API. For the purpose of this guide, we will use a simple notification schema shown in the following code block.

```json
{ 
    "type":"record",
    "name":"ExampleNotification",
    "namespace":"org.kaaproject.kaa.schema.sample.notification",
    "fields":[ 
        { 
            "name":"message",
            "type":"string"
        }
    ]
}
```

#### Notification topics


Notifications in Kaa are organized into topics. Each topic may be associated with one or more endpoint groups. To subscribe to a specific notification, endpoints must belong to one or more endpoint groups that are associated with the corresponding notification topic.
Topics can be mandatory or optional. Mandatory topic notifications are delivered in an enforced manner. Optional topics require subscription. It is responsibility of the client code to add notification listeners and subscribe to optional topics.
You can manage notification topics via [Admin UI]() or [REST API]().

***Once created, a notification topic does not impact any endpoints. To deliver notifications to some endpoint, at first you need to assign the topic to an endpoint group containing this endpoint via [Admin UI]() or [REST API]().***


Assuming that you have created custom endpoint groups from the [Using endpoint groups guide](), it would be logical to create and assign the following topics:

|                           | Endpoint Group Name |||||
|---------------------------|-----------------------|-------------------------|--------------------------|-----------------|----------------------------|
| Topic id                  | All                   | Android Froyo endpoints | Android endpoints        | iOS 8 endpoints | 3.0 RC1 QA group endpoints |
| Android notifications     | false                 | true                    | true                     | false           | false                      |
| iOS 8 notifications       | false                 | false                   | false                    | true            | false                      |
| All devices notifications | true                  | false                   | false                    | false           | false                      |


#### Sending notifications
To send a notification, you can issue the REST API request or use Admin UI.

### Coding
This section provides code samples which illustrate practical usage of notifications in Kaa.

#### Get available topics
To get a list of available topics, do the following:


<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Java-9">Java</a></li>
  <li><a data-toggle="tab" href="#C_plus_plus-9">C++</a></li>
  <li><a data-toggle="tab" href="#C-9">C</a></li>
  <li><a data-toggle="tab" href="#Objective-C-9">Objective-C</a></li>
</ul>

<div class="tab-content">
<div id="Java-9" class="tab-pane fade in active" markdown="1" >

```java
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.DesktopKaaPlatformContext;
import org.kaaproject.kaa.common.endpoint.gen.Topic;
...
    KaaClient kaaClient = Kaa.newClient(new DesktopKaaPlatformContext())
    // Start Kaa client
    kaaClient.start()
...
 
    List<Topic> topics = kaaClient.getTopics();
 
    for (Topic topic : topics) {
        System.out.printf("Id: %s, name: %s, type: %s"
                , topic.getId(), topic.getName(), topic.getSubscriptionType());
    }
```

</div><div id="C_plus_plus-9" class="tab-pane fade" markdown="1" >

```c++
#include <iostream>
 
#include <kaa/Kaa.hpp>
#include <kaa/logging/LoggingUtils.hpp>
 
using namespace kaa;
 
...
// Create an endpoint instance
auto kaaClient = Kaa::newClient();
// Start an endpoint
kaaClient->start();
 
// Get available topics
const auto& topics = kaaClient->getTopics();
for (const auto& topic : topics) {
    std::cout << "Id: " << topic.id << ", name: " << topic.name
              << ", type: " << LoggingUtils::TopicSubscriptionTypeToString(topic.subscriptionType) << std::endl;
}
```

</div><div id="C-9" class="tab-pane fade" markdown="1" >

```c
#include <kaa/platform/kaa_client.h>
#include <kaa/kaa_notification_manager.h>
 
kaa_client_t *kaa_client = /* ... */;
 
void on_topic_list_uploaded(void *context, kaa_list_t *topics)
{
    kaa_list_node_t *it = kaa_list_begin(topics);
    while (it) {
         kaa_topic_t *topic = (kaa_topic_t *) kaa_list_get_data(it);
         printf("Id: %lu, name: %s, type: %s\n", topic->id, topic->name, (topic->subscription_type == OPTIONAL_SUBSCRIPTION)? "OPTIONAL":"MANDATORY");  
         it = kaa_list_next(it);
    }
}
 
kaa_list_t *topics_list = NULL;
kaa_error_t error_core = kaa_get_topics(kaa_client_get_context(kaa_client)->notification_manager, &topics_list);
on_topic_list_uploaded(NULL, topics_list);
```

</div><div id="Objective-C-9" class="tab-pane fade" markdown="1" >

```objective-c
#import <Kaa/Kaa.h>
...
    id<KaaClient> kaaClient = [Kaa client]
...
    // Start Kaa client
    [kaaClient start]
...
 
    NSArray *topics = [kaaClient getTopics];
 
    for (Topic *topic in topics) {
        NSLog(@"%lld %@ %u", topic.id, topic.name, topic.subscriptionType);
    }
```

</div></div>

#### Subscribe to updates on available topics
To receive updates for the available topics list, add at least one listener as shown in the following code block (the number of listeners is not limited):

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Java-10">Java</a></li>
  <li><a data-toggle="tab" href="#C_plus_plus-10">C++</a></li>
  <li><a data-toggle="tab" href="#C-10">C</a></li>
  <li><a data-toggle="tab" href="#Objective-C-10">Objective-C</a></li>
</ul>

<div class="tab-content">
<div id="Java-10" class="tab-pane fade in active" markdown="1" >

```java
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.KaaDesktop;
import org.kaaproject.kaa.client.notification.NotificationManager;
import org.kaaproject.kaa.client.notification.NotificationTopicListListener;
import org.kaaproject.kaa.common.endpoint.gen.Topic;
...
// Add listener
kaaClient.addTopicListListener(new NotificationTopicListListener() {
    @Override
    public void onListUpdated(List<Topic> topics) {
        for (Topic topic : topics) {
            System.out.printf("Id: %s, name: %s, type: %s",
            topic.getId(), topic.getName(), topic.getSubscriptionType());
    }
}});
...
// Remove listener
kaaClient.removeTopicListListener(someOtherTopicUpdateListener);
```

</div><div id="C_plus_plus-10" class="tab-pane fade" markdown="1" >

```c++
#include <iostream>
#include <memory>
 
#include <kaa/Kaa.hpp>
#include <kaa/logging/LoggingUtils.hpp>
#include <kaa/notification/INotificationTopicListListener.hpp>
 
using namespace kaa;
class NotificationTopicListListener : public INotificationTopicListListener {
public:
    virtual void onListUpdated(const Topics& topics)
    {
        for (const auto& topic : topics) {
            std::cout << "Id: " << topic.id << ", name: " << topic.name
              << ", type: " << LoggingUtils::TopicSubscriptionTypeToString(topic.subscriptionType) << std::endl;
        }
    }
};
...
// Create a listener which receives the list of available topics.
std::unique_ptr<NotificationTopicListListener> topicListListener(new NotificationTopicListListener());
 
// Add a listener
kaaClient->addTopicListListener(*topicListListener);
// Remove a listener
kaaClient->removeTopicListListener(*topicListListener);
```

</div><div id="C-10" class="tab-pane fade" markdown="1" >

```c
#include <kaa/kaa_notification_manager.h>
#include <kaa/platform/ext_notification_receiver.h>
kaa_topic_listener_t topic_listener = { &on_topic_list_uploaded, NULL };
uint32_t topic_listener_id = 0;
 
// Add listener
kaa_error_t error_code = kaa_add_topic_list_listener(kaa_client_get_context(kaa_client)->notification_manager, &topic_listener, &topic_listener_id);
 
// Remove listener
error_code = kaa_remove_topic_list_listener(kaa_client_get_context(kaa_client)->notification_manager, &topic_listener_id);
```

</div><div id="Objective-C-10" class="tab-pane fade" markdown="1" >

```objective-c
#import <Kaa/Kaa.h>
 
@interface ViewController() <NotificationTopicListDelegate>
 
...
    // Add listener
    [self.kaaClient addTopicListDelegate:self];
...
 
- (void)onListUpdated:(NSArray *)list {
    for (Topic *topic in topics) {
        NSLog([NSString stringWithFormat:@"%lld %@ %u", topic.id, topic.name, topic.subscriptionType]);
    }
}
...
    // Remove listener
    [self.kaaClient removeTopicListDelegate:self];
```

</div></div>



When subscription changes simultaneously for several topics, the following approach is recommended for performance reasons:
<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Java-11">Java</a></li>
  <li><a data-toggle="tab" href="#C_plus_plus-11">C++</a></li>
  <li><a data-toggle="tab" href="#C-11">C</a></li>
  <li><a data-toggle="tab" href="#Objective-C-11">Objective-C</a></li>
</ul>

<div class="tab-content">
<div id="Java-11" class="tab-pane fade in active" markdown="1" >

```java
import org.kaaproject.kaa.client.notification.NotificationManager;
...
// Do subscription changes with parameter forceSync set to false
kaaClient.subscribeToTopics(Arrays.asList("iOS 8 notifications", "another_optional_topic_id"), false);
kaaClient.unsubscribeFromTopic("boring_optional_topic_id", false);
...
// Add notification listener(s) (optional)
...
// Synchronizes new topic subscriptions.
kaaClient.syncTopicsList();
```

</div><div id="C_plus_plus-11" class="tab-pane fade" markdown="1" >

```c++
#include <kaa/Kaa.hpp>
...
 
// Subscribe to the list of topics and unsubscribe from one topic.
// By setting the second parameter to false an endpoint postpones sending a subscription request till a user calls syncTopicSubscriptions().
 
kaaClient->subscribeOnTopics({"iOS 8 notifications", "another_optional_topic_id"}, false);
kaaClient->unsubscribeFromTopic("boring_optional_topic_id", false);
...
 
// Add notification listener(s) (optional)
 
...
// Sending a subscription request
kaaClient->syncTopicSubscriptions();
```

</div><div id="C-11" class="tab-pane fade" markdown="1" >

```c
#include <kaa/kaa_notification_manager.h>
#include <kaa/platform/ext_notification_receiver.h>
// Assume we have some optional topics
uint64_t topic_ids[] = { 12345, 6789 };
 
// Subscribe to the list of topics and unsubscribe from one topic.
// By setting the second parameter to false an endpoint postpones sending a subscription request till a user calls syncTopicSubscriptions().
kaa_error_t error_code = kaa_subscribe_to_topics(kaa_client_get_context(kaa_client)->notification_manager, topic_ids, sizeof(topic_ids) / sizeof(uint64_t), false);
error_code = kaa_unsubscribe_from_topic(kaa_client_get_context(kaa_client)->notification_manager, topic_ids, false);
 
// Sending a subscription request
error_code = kaa_sync_topic_subscriptions(kaa_client_get_context(kaa_client)->notification_manager);
```

</div><div id="Objective-C-11" class="tab-pane fade" markdown="1" >

```objective-c
#import <Kaa/Kaa.h>
 
...
 
    // Do subscription changes with parameter forceSync set to false
    NSArray *topicIds = @[@"iOS 8 notifications", @"another_optional_topic_id"];
    [kaaClient subscribeToTopicsWithIDs:topicIds forceSync:NO];
    [kaaClient unsubscribeFromTopicWithId:@"boring_optional_topic_id" forceSync:NO];
    ...
     
    // Add notification listener(s) (optional)
    ...
     
    // Synchronizes new topic subscriptions.
    [kaaClient syncTopicsList];
```

</div></div>

#### Default notification listener
There are two types of topic notification listeners: the default and topic specific. To receive notifications, add at least one default listener (the number of default listeners is not limited) as shown in the following code block. As a result, the listener will receive notifications from all topics (mandatory topics, as well as optional topics having been subscribed to). 

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Java-12">Java</a></li>
  <li><a data-toggle="tab" href="#C_plus_plus-12">C++</a></li>
  <li><a data-toggle="tab" href="#C-12">C</a></li>
  <li><a data-toggle="tab" href="#Objective-C-12">Objective-C</a></li>
</ul>

<div class="tab-content">
<div id="Java-12" class="tab-pane fade in active" markdown="1" >

```java
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.notification.NotificationListener;
import org.kaaproject.kaa.schema.sample.notification.ExampleNotification;
...
public class BasicNotificationListener implements NotificationListener {
 
    @Override
    public void onNotification(String topicId, ExampleNotification notification) {
        System.out.println("Received a notification: " + notification.toString());
    }
}
...
BasicNotificationListener listener = new BasicNotificationListener();
// Add listener
kaaClient.addNotificationListener(listener);
...
// Remove listener
kaaClient.removeNotificationListener(listener);
```

</div><div id="C_plus_plus-12" class="tab-pane fade" markdown="1" >

```c++
#include <cstdint>
#include <iostream>
#include <memory>
 
#include <kaa/notification/INotificationListener.hpp>
 
using namespace kaa;
 
class BasicNotificationListener : public INotificationListener {
public:
    virtual void onNotification(const std::int64_t topicId, const KaaNotification& notification)
    {
        std::cout << "Received notification on topic: id '"<< topicId << "', message: " << notification.message << std::endl;
    }
};
...
 
// Creates the listener which receives notifications on all available topics.
std::unique_ptr<BasicNotificationListener> listener(new BasicNotificationListener());
 
// Adds listener
kaaClient->addNotificationListener(*listener);
 
// Remove listener
kaaClient->removeNotificationListener(*listener);
```

</div><div id="C-12" class="tab-pane fade" markdown="1" >

```c
#include <kaa/kaa_notification_manager.h>
#include <kaa/platform/ext_notification_receiver.h>
void on_notification(void *context, uint64_t *topic_id, kaa_notification_t *notification)
{
    printf("Received notification on topic %llu: message='%s'\n", topic_id, notification->message);
}
 
kaa_notification_listener_t notification_listener = { &on_notification, NULL };
uint32_t notification_listener_id = 0;
 
// Add listener
kaa_error_t error_code = kaa_add_notification_listener(kaa_client_get_context(kaa_client)->notification_manager, &notification_listener, &notification_listener_id);
 
// Remove listener
error_code = kaa_remove_notification_listener(kaa_context_->notification_manager, &notification_listener_id);
```

</div><div id="Objective-C-12" class="tab-pane fade" markdown="1" >

```objective-c
#import <Kaa/Kaa.h>
 
@interface ViewController () <NotificationDelegate>
 
...
 
 
- (void)onNotification:(KAASampleNotification *)notification withTopicId:(NSString *)topicId {
    NSLog(@"Received a notification: %@", notification);
}
 
    // Add listener
    [kaaClient addNotificationDelegate:self];
    ...
    // Remove listener
    [kaaClient removeNotificationDelegate:self];
```

</div></div>

#### Topic specific notification listener
To receive notifications on some specific topic (either mandatory or optional), you can use topic specific listeners (the number of listeners per topic is not limited) instead of the default listener. To create a topic specific listener, do the following: 

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Java-13">Java</a></li>
  <li><a data-toggle="tab" href="#C_plus_plus-13">C++</a></li>
  <li><a data-toggle="tab" href="#C-13">C</a></li>
  <li><a data-toggle="tab" href="#Objective-C-13">Objective-C</a></li>
</ul>

<div class="tab-content">
<div id="Java-13" class="tab-pane fade in active" markdown="1" >

```java
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.schema.sample.notification.Notification;
...
BasicNotificationListener specificListener = new BasicNotificationListener();
// Add listener
kaaClient.addNotificationListener("All devices notifications", listener);
...
// Remove listener
kaaClient.removeNotificationListener("All devices notifications", listener);
```

</div><div id="C_plus_plus-13" class="tab-pane fade" markdown="1" >

```c++
#include <iostream>
#include <string>
#include <memory>
 
#include <kaa/Kaa.hpp>
 
...
 
// Create the listener which receives notifications on the specified optional topic.
std::unique_ptr<BasicNotificationListener> listener(new BasicNotificationListener());
 
// Add listener
kaaClient->addNotificationListener("All devices notifications", *listener);
 
// Remove listener
kaaClient->removeNotificationListener("All devices notifications", *listener);
```

</div><div id="C-13" class="tab-pane fade" markdown="1" >

```c
#include <kaa/kaa_notification_manager.h>
#include <kaa/platform/ext_notification_receiver.h>
 
// Assume we have some topic
uint64_t topic_id = 12345;
kaa_notification_listener_t notification_listener = { &on_notification, NULL };
uint32_t notification_listener_id = 0;
 
 
// Add listener
kaa_error_t error_code = kaa_add_optional_notification_listener(kaa_client_get_context(kaa_client)->notification_manager, &notification_listener, &topic_id, &notification_listener_id);
 
// Remove listener
error_code = kaa_remove_optional_notification_listener(kaa_client_get_context(kaa_client)->notification_manager, &topic_id, &notification_listener_id);
```

</div><div id="Objective-C-13" class="tab-pane fade" markdown="1" >

```objective-c
#import <Kaa/Kaa.h>
...
 
// Add listener
[kaaClient addNotificationDelegate:self forTopicId:@"All devices notifications"];
...
// Remove listener
[kaaClient removeNotificationDelegate:self forTopicId:@"All devices notifications"];
```

</div></div>

#### Subscribe to optional topics
To receive notifications on some optional topic, at first subscribe to that topic as shown in the following code block:
<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Java-14">Java</a></li>
  <li><a data-toggle="tab" href="#C_plus_plus-14">C++</a></li>
  <li><a data-toggle="tab" href="#C-14">C</a></li>
  <li><a data-toggle="tab" href="#Objective-C-14">Objective-C</a></li>
</ul>

<div class="tab-content">
<div id="Java-14" class="tab-pane fade in active" markdown="1" >

```java
import org.kaaproject.kaa.client.KaaClient;
...
// Add notification listener(s) (optional)
...
// Subscribe
kaaClient.subscribeToTopic("Android notifications", true);
...
// Unsubscribe. All added listeners will be removed automatically
kaaClient.unsubscribeFromTopic("Android notifications", true);
```

</div><div id="C_plus_plus-14" class="tab-pane fade" markdown="1" >

```c++
#include <kaa/Kaa.hpp>
 
using namespace kaa;
 
...
 
// Add notification listener(s) (optional)
 
// Subscribe
kaaClient->subscribeToTopic("Android notifications");
 
// Unsubscribe
kaaClient->unsubscribeFromTopic("Android notifications");
```

</div><div id="C-14" class="tab-pane fade" markdown="1" >

```c
#include <kaa/kaa_notification_manager.h>
#include <kaa/platform/ext_notification_receiver.h>
// Assume we have some optional topic
uint64_t topic_id = 12345;
 
// Subcribe
kaa_error_t error_code = kaa_subscribe_to_topic(kaa_client_get_context(kaa_client)->notification_manager, &topic_id, true);
 
// Unsubscribe. All added listeners will be removed automatically
error_code = kaa_unsubscribe_from_topic(kaa_client_get_context(kaa_client)->notification_manager, &topic_id, true);
```

</div><div id="Objective-C-14" class="tab-pane fade" markdown="1" >

```objective-c
#import <Kaa/Kaa.h>
...
// Add notification listener(s) (optional)
 
// Subscribe
[kaaClient subscribeToTopicWithId:@"iOS notifications" forceSync:YES];
...
// Unsubscribe. All added listeners will be removed automatically
[kaaClient unsubscribeFromTopicWithId:@"iOS notifications" forceSync:YES];
```

</div></div>

You can work with a list of optional topics in a similar way as with a list of available topics.

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Java-15">Java</a></li>
  <li><a data-toggle="tab" href="#C_plus_plus-15">C++</a></li>
  <li><a data-toggle="tab" href="#C-15">C</a></li>
  <li><a data-toggle="tab" href="#Objective-C-15">Objective-C</a></li>
</ul>

<div class="tab-content">
<div id="Java-15" class="tab-pane fade in active" markdown="1" >

```java
// Add notification listener(s) (optional)
...
// Subscribe
kaaClient.subscribeToTopics(Arrays.asList("iOS 8 notifications", "another_optional_topic_id"), true);
...
// Unsubscribe
kaaClient.unsubscribeFromTopics(Arrays.asList("iOS 8 notifications", "another_optional_topic_id"), true);
```

</div><div id="C_plus_plus-15" class="tab-pane fade" markdown="1" >

```c++
#include <kaa/Kaa.hpp>
...
// Add notification listener(s) (optional)
 
// Subscribe
kaaClient->subscribeToTopics({"iOS 8 notifications", "another_optional_topic_id"});
 
// Unsubscribe
kaaClient->unsubscribeFromTopics({"iOS 8 notifications", "another_optional_topic_id"});
```

</div><div id="C-15" class="tab-pane fade" markdown="1" >

```c
#include <kaa/kaa_notification_manager.h>
#include <kaa/platform/ext_notification_receiver.h>
 
// Assume we have some optional topics
uint64_t topic_ids[] = { 12345, 6789 };
 
// Subscribe
kaa_error_t error_code = kaa_subscribe_to_topics(kaa_client_get_context(kaa_client)->notification_manager, topic_ids, sizeof(topic_ids) / sizeof(uint64_t), true);
 
// Unsubscribe
error_code = kaa_unsubscribe_from_topics(kaa_client_get_context(kaa_client)->notification_manager, topic_ids, topics_count, true);
```

</div><div id="Objective-C-15" class="tab-pane fade" markdown="1" >

```objective-c
// Add notification listener(s) (optional)
...
// Subscribe
[kaaClient subscribeToTopicsWithIDs:@[@"iOS 8 notifications", @"another_optional_topic_id"] forceSync:YES];
...
// Unsubscribe
[kaaClient unsubscribeFromTopicsWithIDs:@[@"iOS 8 notifications", @"another_optional_topic_id"] forceSync:YES];
```

</div></div>
