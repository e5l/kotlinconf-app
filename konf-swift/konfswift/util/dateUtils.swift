import Foundation

fileprivate var DATE_FORMATTER: DateFormatter = {
    // NSDateFormatter
    let formatter = DateFormatter()

    // @property (null_resettable, copy) NSString *dateFormat;
    formatter.dateFormat = "dd.MM HH:mm a"

    // @property (null_resettable, copy) NSTimeZone *timeZone;
    formatter.timeZone = TimeZone(abbreviation: "UTC")
    return formatter
}()

fileprivate var TIME_FORMATTER: DateFormatter = {
    let formatter = DateFormatter()
    formatter.dateFormat = "HH:mm a"
    formatter.timeZone = TimeZone(abbreviation: "UTC")
    return formatter
}()

fileprivate var DATE_PARSER: DateFormatter = {
    let formatter = DateFormatter()
    formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss"
    formatter.timeZone = TimeZone(abbreviation: "UTC")
    return formatter
}()

fileprivate var DEFAULT_DATE_STRING: String = {
    // NSDate
    // - (instancetype)initWithTimeIntervalSinceReferenceDate:(NSTimeInterval)ti NS_DESIGNATED_INITIALIZER;
    let date = Date(timeIntervalSinceReferenceDate: 0)
    return DATE_FORMATTER.string(from: date)
}()

func parseDate(from str: String) -> Date? {
    return DATE_PARSER.date(from: str)
}

func renderDate(_ date: Date?) -> String {
    if let nonNullDate = date {
        return DATE_FORMATTER.string(from: nonNullDate)
    } else {
        return DEFAULT_DATE_STRING
    }
}

func renderDates(startDate: Date?, endDate: Date?) -> String {
    // - (instancetype)initWithTimeIntervalSince1970:(NSTimeInterval)secs;
    let _startDate = startDate ?? Date(timeIntervalSince1970: 0)
    let _endDate = endDate ?? Date(timeIntervalSince1970: 0)

    return DATE_FORMATTER.string(from: _startDate) + " – " + TIME_FORMATTER.string(from: _endDate)
}
