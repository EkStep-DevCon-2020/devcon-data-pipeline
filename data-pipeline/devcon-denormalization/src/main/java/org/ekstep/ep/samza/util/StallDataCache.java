package org.ekstep.ep.samza.util;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.Map;

public class StallDataCache {

    private Map<String, String> stallDataMap;
    private Map<String, String> ideaDataMap;


    public StallDataCache() {
        Type mapType = new TypeToken<Map<String, Object>>() {}.getType();

        String stallData = "{\"STA1\": \"Creation\", \"STA2\": \"School\", \"STA3\": \"Games\", \"STA4\": \"Devops\", \"STA5\": \"Analytics\"}";
        this.stallDataMap = new Gson().fromJson(stallData, mapType);

        // String ideaData = "{'IDE1': 'Taxonomy Alignment', 'IDE2': 'Crossword Generation', 'IDE3': 'AR/VR Content', 'IDE4': 'Interactive Videos', 'IDE5': 'Live Coaching Session', 'IDE6': 'Creation Anywhere', 'IDE7': 'Video WIKI', 'IDE8': 'Auto Curation', 'IDE9': 'Attendance tracking', 'IDE10': 'Syllabus coverage', 'IDE11': 'Child profiles shared between teachers and parents', 'IDE12': 'Analytics on student data', 'IDE13': 'Embeddable player in other apps', 'IDE14': 'Player within content', 'IDE15': 'Voice command mode', 'IDE16': 'Accessibility', 'IDE17': 'Smart consumption', 'IDE18': 'Smart wall', 'IDE19': 'Home assessment', 'IDE20': 'Wordnet', 'IDE21': 'Reading Quality Index', 'IDE22': 'Hangman', 'IDE23': 'Multiplayer Game', 'IDE24': 'Adoptive Game', 'IDE25': 'Rock paper scissors', 'IDE26': 'Beat the Bot', 'IDE27': 'DISHA - Career Guidance', 'IDE28': 'Magic Mirror', 'IDE29': 'Entry Gate', 'IDE30': 'Water Pot', 'IDE31': 'GitOps', 'IDE32': 'AIOps', 'IDE33': 'Anamoly Detection', 'IDE34': 'Publish reports from Superset', 'IDE35': 'Voice based querying on Druid', 'IDE36': 'Drill down on chart data', 'IDE37': 'Error Dashboards and Devcon Dashboard'}";
        String ideaData = "{\"IDE1\": \"Taxonomy Alignment\", \"IDE2\": \"Crossword Generation\", \"IDE3\": \"AR/VR Content\", \"IDE4\": \"Interactive Videos\", \"IDE5\": \"Live Coaching Session\", \"IDE6\": \"Creation Anywhere\", \"IDE7\": \"Video WIKI\", \"IDE8\": \"Auto Curation\", \"IDE9\": \"Attendance tracking\", \"IDE10\": \"Syllabus coverage\", \"IDE11\": \"Child profiles shared between teachers and parents\", \"IDE12\": \"Analytics on student data\", \"IDE13\": \"Embeddable player in other apps\", \"IDE14\": \"Player within content\", \"IDE15\": \"Voice command mode\", \"IDE16\": \"Accessibility\", \"IDE17\": \"Smart consumption\", \"IDE18\": \"Smart wall\", \"IDE19\": \"Home assessment\", \"IDE20\": \"Wordnet\", \"IDE21\": \"Reading Quality Index\", \"IDE22\": \"Hangman\", \"IDE23\": \"Multiplayer Game\", \"IDE24\": \"Adoptive Game\", \"IDE25\": \"Rock paper scissors\", \"IDE26\": \"Beat the Bot\", \"IDE27\": \"DISHA - Career Guidance\", \"IDE28\": \"Magic Mirror\", \"IDE29\": \"Entry Gate\", \"IDE30\": \"Water Pot\", \"IDE31\": \"GitOps\", \"IDE32\": \"AIOps\", \"IDE33\": \"Anamoly Detection\", \"IDE34\": \"Publish reports from Superset\", \"IDE35\": \"Voice based querying on Druid\", \"IDE36\": \"Drill down on chart data\", \"IDE37\": \"Error Dashboards and Devcon Dashboard\"}";
        this.ideaDataMap = new Gson().fromJson(ideaData, mapType);
    }

    public String getStallName(String stallId) {
        return stallDataMap.getOrDefault(stallId, stallId);
    }

    public String getIdeaName(String idealId) {
        return ideaDataMap.getOrDefault(idealId, idealId);
    }
}
