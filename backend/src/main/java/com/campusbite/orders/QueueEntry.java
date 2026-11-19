package com.campusbite.orders;

/**
 * One line of an outlet's shared queue board. Deliberately contains no names, emails or items,
 * so every customer can see how long the line is without seeing other customers' details.
 */
public record QueueEntry(long queueNumber, long position, String status, boolean mine, int estimatedWaitMinutes) {
}
