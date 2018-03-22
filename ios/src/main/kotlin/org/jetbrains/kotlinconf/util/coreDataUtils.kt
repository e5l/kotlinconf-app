package org.jetbrains.kotlinconf.util

import platform.CoreData.*
import platform.Foundation.*

fun NSManagedObjectContext.deleteAll(entityName: String) {
    val fetchRequest = NSFetchRequest.fetchRequestWithEntityName(entityName)
    fetchRequest.includesPropertyValues = false

    for (item in executeFetchRequest(fetchRequest).toList<NSManagedObject>()) {
        deleteObject(item)
    }
}

fun NSManagedObjectContext.saveRecursively() {
    this.save()

    var current: NSManagedObjectContext? = this.parentContext
    while (current != null) {
        val context = current
        var error: NSError? = null

        context.performAndWait {
            try {
                context.save()
            } catch (e: NSErrorException) {
                error = e.error
            }
        }

        error?.let { throw NSErrorException(it) }
        current = current.parentContext
    }
}

fun NSURLSession.dataTask(
        request: NSURLRequest,
        handler: (NSData?, NSURLResponse?, NSError?) -> Unit
): NSURLSessionDataTask = dataTaskWithRequest(request, handler)

fun NSManagedObjectContext.executeFetchRequest(request: NSFetchRequest): NSArray {
    return nsTry { errorPtr -> executeFetchRequest(request, error = errorPtr)!! }
}

fun NSManagedObjectContext.save() {
    nsTry { errorPtr -> save(error = errorPtr) }
}
