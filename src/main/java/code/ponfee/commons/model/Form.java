package code.ponfee.commons.model;

import java.util.List;

/**
 * 表单
 * 
 * @author Ponfee
 */
public class Form implements java.io.Serializable {

    private static final long serialVersionUID = 3335254023919017587L;

    private List<Parameter> parameters;

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public static class Parameter implements java.io.Serializable {
        private static final long serialVersionUID = 4322704347383719451L;

        private String  name;     // 参数名
        private Type    type;     // 表单类型
        private String  label;    // 标签名
        private boolean required; // 是否必填
        private boolean multiple; // 是否可多选

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Type getType() {
            return type;
        }

        public void setType(Type type) {
            this.type = type;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public boolean isRequired() {
            return required;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }

        public boolean isMultiple() {
            return multiple;
        }

        public void setMultiple(boolean multiple) {
            this.multiple = multiple;
        }
    }

    public enum Type {
        INPUT, PASSWORD, TEXTAREA, RADIO,  //
        CHECKBOX, SELECT, COMBOX, DATEBOX, // 
        ;
    }

}
