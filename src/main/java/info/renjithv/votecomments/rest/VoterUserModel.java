package info.renjithv.votecomments.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "voter")
@XmlAccessorType(XmlAccessType.FIELD)
public class VoterUserModel {
    @XmlElement(name = "name")
    private final String name;

    @XmlElement(name = "displayName")
    private final String displayName;

    public VoterUserModel(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }
}
