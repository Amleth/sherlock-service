package fr.cnrs.iremus.sherlock

import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification

import javax.inject.Inject

@MicronautTest
class TwoUsersAndAE32 extends Specification {
    @Inject
    @Client("/")
    RxHttpClient client
    @Inject
    Common common
    @Inject
    DateService dateService;
    @Inject
    Sherlock sherlock;

    static final String E55_ABONNEMENT = "6db65be7-65f8-42bd-83bc-6fbf2148dc4a"
    static final String U1 = ""
    static final String U2 = ""

    void 'test it works'() {
        when:
        common.fuck()

        // Soient deux utilisateurs U1 & U2. U2 crée un E32 (v) et le peuple de concepts (x, y, z).



        // U1 veut utiliser v et s'y abonne donc.

        then:
        true
    }
}
