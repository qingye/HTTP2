# HTTP2
A server HTTP2 library

Based on standards by the Internet Engineering Task Force (IETF), specified [here](https://tools.ietf.org/html/rfc7540).

Disclaimer: This is not in any way a complete or reliable library, and should not be used for anything important.

## Introduction
This library was developed as a school project for NTNU Trondheim. 
It is designed to implement very basic HTTP/2 functionality, and includes an example of usage.
The example uses HTTP/2 over TLS, but the library can in theory be used with other protocols.

## Functionality
This library contains the following:
* Frames
    * Classes for all frame types.
    * Easy to use constructors.
    * Constructors for creating frames from raw byte data.
    * Simple methods for converting frame objects to raw byte data.
    * Enums for frame types, settings and error codes, \
      and an interface with constants and various methods for frame flags.
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

These are some of the biggest improvements that HTTP/2 has over HTTP/1.1,
meaning that although we have technically implemented HTTP/2, it is not much, 
if at all, faster than HTTP/1.1, and less secure.


## Example
The _example_ package includes an example of the usage of this library. 
It is a simple server that creates connections and shows a simple HTML document in the browser.

## Dependencies
This library uses the [twitter/hpack](https://github.com/twitter/hpack) library for HPACK compression of headers. 

## Installation


## Documentation
Javadoc is available [here]().