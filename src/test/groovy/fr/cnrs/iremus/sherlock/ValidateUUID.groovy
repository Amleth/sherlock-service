package fr.cnrs.iremus.sherlock

class ValidateUUID {
    static boolean isValid(final String uuid) {
        def matcher = uuid =~ /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/
        return matcher.matches()
    }
}