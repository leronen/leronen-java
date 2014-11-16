package leronen.tools;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;


/** Parse repĺies from mavencentral JSON queries. Quite simply: read STDIN, write STDOUT. */
public class MavenCentralJSONParser {
    public static void main(String[] args) throws IOException {
        String input = util.IOUtils.readStream(System.in);
        JSONObject json = new JSONObject(input);
        JSONObject response = json.getJSONObject("response");
        JSONArray docs = response.getJSONArray("docs");
        System.out.println("GROUP_ID\tARTIFACT_ID\tLATEST_VERSION\tVERSION_COUNT\tTIMESTAMP\tREPOSITORY_ID\tID\tLATEST_VERSION_ID");
        for (int i=0; i<docs.length(); i++) {
            JSONObject docJSON = docs.getJSONObject(i);
            Doc doc = new Doc(docJSON);
            System.out.println(doc.getGroupId() + "\t" +
                               doc.getArtifactId() + "\t" +
                               doc.getLatestVersion() + "\t" +
                               doc.getVersionCount() + "\t" +
                               doc.getTimeStamp() + "\t" +
                               doc.getRepositoryId() + "\t" +
                               doc.getId() + "\t" +
                               doc.getId() + ":" + doc.getLatestVersion());
        }
    }

    private static class Doc {
        JSONObject data;

        Doc(JSONObject data) {
            this.data = data;
        }
        private String get(String key) {
            return data.getString(key);
        }

        @Override
        public String toString() {
            return "Doc [getLatestVersion()=" + getLatestVersion()
                    + ", getArtifactId()=" + getArtifactId()
                    + ", getGroupId()=" + getGroupId() + ", getVersionCount()="
                    + getVersionCount() + ", getType()=" + getType()
                    + ", getTimeStamp()=" + getTimeStamp()
                    + ", getRepositoryId()=" + getRepositoryId() + ", getId()="
                    + getId() + "]";
        }
        public String getLatestVersion() {
            return get("latestVersion");
        }

        public String getArtifactId() {
            return get("a");
        }

        public String getGroupId() {
            return get("g");
        }

        public int getVersionCount() {
            return data.getInt("versionCount");
        }

        public String getType() {
            return get("p");
        }

        public long getTimeStamp() {
            return data.getLong("timestamp");
        }

        public String getRepositoryId() {
            return get("repositoryId");
        }

        public String getId() {
            return get("id");
        }
    }
}
