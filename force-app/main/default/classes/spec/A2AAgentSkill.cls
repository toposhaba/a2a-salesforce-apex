/**
 * An agent skill
 * Converted from Java io.a2a.spec.AgentSkill
 */
public class A2AAgentSkill implements A2ASerializable {
    public String id { get; set; }
    public String name { get; set; }
    public String description { get; set; }
    public List<String> tags { get; set; }
    public List<String> examples { get; set; }
    public List<String> inputModes { get; set; }
    public List<String> outputModes { get; set; }
    
    public A2AAgentSkill() {
        // Default constructor
        this.tags = new List<String>();
    }
    
    public A2AAgentSkill(String id, String name, String description, List<String> tags,
                        List<String> examples, List<String> inputModes, List<String> outputModes) {
        A2AAssert.checkNotNullParam('description', description);
        A2AAssert.checkNotNullParam('id', id);
        A2AAssert.checkNotNullParam('name', name);
        A2AAssert.checkNotNullParam('tags', tags);
        
        this.id = id;
        this.name = name;
        this.description = description;
        this.tags = tags;
        this.examples = examples;
        this.inputModes = inputModes;
        this.outputModes = outputModes;
    }
    
    public Map<String, Object> serialize() {
        Map<String, Object> result = new Map<String, Object>();
        if (id != null) result.put('id', id);
        if (name != null) result.put('name', name);
        if (description != null) result.put('description', description);
        if (tags != null && !tags.isEmpty()) result.put('tags', tags);
        if (examples != null && !examples.isEmpty()) result.put('examples', examples);
        if (inputModes != null && !inputModes.isEmpty()) result.put('inputModes', inputModes);
        if (outputModes != null && !outputModes.isEmpty()) result.put('outputModes', outputModes);
        return result;
    }
    
    public static A2AAgentSkill deserialize(Map<String, Object> jsonMap) {
        if (jsonMap == null) return null;
        
        A2AAgentSkill skill = new A2AAgentSkill();
        skill.id = (String) jsonMap.get('id');
        skill.name = (String) jsonMap.get('name');
        skill.description = (String) jsonMap.get('description');
        
        List<Object> tagsList = (List<Object>) jsonMap.get('tags');
        if (tagsList != null) {
            skill.tags = new List<String>();
            for (Object tag : tagsList) {
                skill.tags.add((String) tag);
            }
        }
        
        List<Object> examplesList = (List<Object>) jsonMap.get('examples');
        if (examplesList != null) {
            skill.examples = new List<String>();
            for (Object example : examplesList) {
                skill.examples.add((String) example);
            }
        }
        
        List<Object> inputModesList = (List<Object>) jsonMap.get('inputModes');
        if (inputModesList != null) {
            skill.inputModes = new List<String>();
            for (Object mode : inputModesList) {
                skill.inputModes.add((String) mode);
            }
        }
        
        List<Object> outputModesList = (List<Object>) jsonMap.get('outputModes');
        if (outputModesList != null) {
            skill.outputModes = new List<String>();
            for (Object mode : outputModesList) {
                skill.outputModes.add((String) mode);
            }
        }
        
        return skill;
    }
    
    // Builder pattern implementation
    public class Builder {
        private String id;
        private String name;
        private String description;
        private List<String> tags;
        private List<String> examples;
        private List<String> inputModes;
        private List<String> outputModes;
        
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder tags(List<String> tags) {
            this.tags = tags;
            return this;
        }
        
        public Builder examples(List<String> examples) {
            this.examples = examples;
            return this;
        }
        
        public Builder inputModes(List<String> inputModes) {
            this.inputModes = inputModes;
            return this;
        }
        
        public Builder outputModes(List<String> outputModes) {
            this.outputModes = outputModes;
            return this;
        }
        
        public A2AAgentSkill build() {
            return new A2AAgentSkill(id, name, description, tags, examples, inputModes, outputModes);
        }
    }
}