# Scala / Akka-http authenticator sample
##### Maintainer: [Tomer Raz](qtomerr@gmail.com)

## Overview
This app has three fields of responsibility:

* User management
* User authentication
* Session management

It has no CORS support, and is only meant to be
 accessed by a "sponsor" application, which should also handle permission checking.
 Users should not interact with the authenticator directly.

## Usage

Think about the authenticator as a loosely-coupled authentication module.
Upon successful authentication, the authenticator will generate a session entry in the configured volatile storage
and return the key to the sponsor app.

It is the sponsor app's responsibility to load permissions unto the
session created by the authenticator, as well as return the session ID
in cookie format so that it is represented on the client-side.

Upon attempting to access a protected resource, the sponsor app should
access the volatile storage directly, pull all permissions under the
user's session ID and proceed accordingly. 

## Routes

* Alive check

```
GET /alive
```

* Basic diagnostics

```
POST /alive
```

#### User management

* Register new user:

```
POST /users/create
Content-Type: application/json

{
  "userName": <username>,
  "password": <password>,
  "displayName": <display name>,
  "isActive": <true/false>
}
```

* Update existing user:

```
POST /users/update
Content-Type: application/json

{
  "userName": <username>,
  "changes": {
      <any set of params eligible for /users/create>
  }
}
```

* Get all users:

```
GET /users/get
```

* Get specific user:

```
GET /users/get/<userName>
```

#### authentication / Session management

* Login user (authenticate and create empty session):

```
POST /session
Content-Type: application/json

{
  "userName": <username>,
  "password": <password>,
  "appId": <sponsor app id>
}
```

* Logout user (destroy session):

```
DELETE /session?sessionId=<sessionId as returned from /session>
```
