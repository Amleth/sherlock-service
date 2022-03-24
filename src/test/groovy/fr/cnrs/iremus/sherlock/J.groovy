package fr.cnrs.iremus.sherlock

class J {
    static def getOneByType(response, type) {
        return response.find { it?.get("@type")?.get(0) == type.toString() }
    }

    static def getAllByPO(response, predicate, linkedResourceId) {
        return response.findAll { it?.get(predicate.toString())?.get(0)?.get("@id") == linkedResourceId }
    }

    static def getOneByPO(resources, p, o) {
        return resources.find { it[p.toString()][0]["@id"] == o.toString() }
    }

    static def getIri(resource, p) {
        return resource[p.toString()]["@id"][0]
    }

    static def getLiteralValue(resource, p) {
        return resource[p.toString()][0]["@value"]
    }

    static def getLiteralType(resource, p) {
        return resource[p.toString()][0]["@type"]
    }

    static def getLiteralLang(resource, p) {
        return resource[p.toString()][0]["@language"]
    }
}
