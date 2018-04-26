# HTTP2
A server HTTP2 library

Based on standards by the Internet Engineering Task Force (IETF), specified [here](https://tools.ietf.org/html/rfc7540).

Disclaimer: This is not in any way a complete or reliable library, and should not be used for anything important.

## Introduction
This library was developed as a school project for NTNU Trondheim. 
It is designed to implement very basic HTTP/2 functionality, and includes an example of usage.
The example uses HTTP/2 over TLS, but the library can in theory be used with other protocols.
To use HTTP/2 over TLS you will need a certificate. For manual testing we have used a self signed certificate. 
This should not be used in real applications, as it is not secure, but for testing purposes it is good enough.
The certificate we have used is attached in the resources folder.

## Functionality
This library contains the following:
* Frames
    * Classes for all frame types.
    * Easy to use constructors.
    * Constructors for creating frames from raw byte data.
    * Simple methods for converting frame objects to raw byte data.
    * Enums for frame types, settings and error codes, \
      and an interface with constants and various methods for frame flags.
    * HPACK compression in headers and push promise frames. 
* Streams
    * Stream class to keep track of stream objects and their information.
    * Enum for stream states.
* Connections
    * Connection interface and abstract connection to make it easy to implement your own connection.
    * Connection thread to enable concurrent connections.
    * Connection settings to store settings for a specific connection.

## Future work
There are a number of important HTTP/2 functionalities missing from this library, such as
* Frame pushing
* Flow control
* Various security measures
* Testing for frames


## Example
The _example_ package includes an example of the usage of this library. 
It is a simple server that creates connections and shows a simple HTML document in the browser(Only confirmed in chrome).

How to run example.
1. Run main
2. Open webbrowser and go to https://localhost
3. Open chrome://net-internals/ to see behind the scenes client perspective.

## Dependencies
This library uses the [twitter/hpack](https://github.com/twitter/hpack) library for HPACK compression of header block fragments. 

## Installation
1. Clone this repository
2. Install java version 9 or later. Tested on java 10.
2. Install maven and run maven install in the HTTP/2 project folder


## Documentation
Javadoc is available [here](https://rolv-arild.github.io/HTTP2/index.html?overview-summary.html).