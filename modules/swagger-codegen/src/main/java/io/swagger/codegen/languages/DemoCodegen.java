package io.swagger.codegen.languages;

import io.swagger.codegen.CodegenConfig;
import io.swagger.codegen.CodegenType;
import io.swagger.codegen.DefaultCodegen;
import io.swagger.codegen.SupportingFile;
import io.swagger.models.*;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.File;

import java.util.*;

public class DemoCodegen extends DefaultCodegen implements CodegenConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoCodegen.class);
    protected String apiVersion = "1.0.0";
    protected int serverPort = 8080;
    protected String projectName = "swagger-server";
    protected String projectFolder = "src" + File.separator + "main";
    protected String projectTestFolder = "src" + File.separator + "test";
    protected String sourceFolder = projectFolder + File.separator + "java";
    protected String testFolder = projectTestFolder + File.separator + "java";
    protected String apiDocPath = "docs/";
    protected String modelDocPath = "docs/";

    public DemoCodegen() {
        super();
        templateDir = "restApi";
        outputFolder = "generated-code/demoServer";
        apiTestTemplateFiles.clear();
        embeddedTemplateDir = templateDir = "demoServer";
        apiPackage = "io.swagger.api";
        modelPackage = "io.swagger.model";

        modelTemplateFiles.clear();
        apiTemplateFiles.put("api.mustache", ".java");
//        apiTemplateFiles.put("ApisController.mustache", ".java");

        hideGenerationTimestamp = false;

        setReservedWordsLowerCase(
                Arrays.asList(
//                        // used as internal variables, can collide with parameter names
//                        "localVarPath", "localVarQueryParams", "localVarCollectionQueryParams",
//                        "localVarHeaderParams", "localVarFormParams", "localVarPostBody",
//                        "localVarAccepts", "localVarAccept", "localVarContentTypes",
//                        "localVarContentType", "localVarAuthNames", "localReturnType",
//                        "ApiClient", "ApiException", "ApiResponse", "Configuration", "StringUtil",

                        // language reserved words
                        "abstract", "continue", "for", "new", "switch", "assert",
                        "default", "if", "package", "synchronized", "boolean", "do", "goto", "private",
                        "this", "break", "double", "implements", "protected", "throw", "byte", "else",
                        "import", "public", "throws", "case", "enum", "instanceof", "return", "transient",
                        "catch", "extends", "int", "short", "try", "char", "final", "interface", "static",
                        "void", "class", "finally", "long", "strictfp", "volatile", "const", "float",
                        "native", "super", "while", "null")
        );

        languageSpecificPrimitives = new HashSet<String>(
                Arrays.asList(
                        "String",
                        "boolean",
                        "Boolean",
                        "Double",
                        "Integer",
                        "Long",
                        "Float",
                        "Object",
                        "byte[]")
        );
        instantiationTypes.put("array", "ArrayList");
        instantiationTypes.put("map", "HashMap");
        typeMapping.put("date", "Date");
        typeMapping.put("file", "File");

    }

    @Override
    public String getName() {
        return "demo";
    }

    @Override
    public CodegenType getTag() {
        return CodegenType.SERVER;
    }

    @Override
    public String getHelp() {
        return "Generates a Java AWSREST Server application.";
    }

    @Override
    public String toApiName(String name) {
        if (name.length() == 0) {
            return "DefaultApi";
        }
        return camelize(name) + "Api";
    }

    @Override
    public String toApiFilename(String name) {
        return toApiName(name);
    }

    /**
     * Escapes a reserved word as defined in the `reservedWords` array. Handle escaping
     * those terms here.  This logic is only called if a variable matches the reserved words
     *
     * @return the escaped term
     */
    @Override
    public String escapeReservedWord(String name) {
        if (this.reservedWordsMappings().containsKey(name)) {
            return this.reservedWordsMappings().get(name);
        }
        return "_" + name;
    }

    /**
     * Location to write api files.  You can use the apiPackage() as defined when the class is
     * instantiated
     */
    @Override
    public String apiFileFolder() {
        return outputFolder + "/" + sourceFolder + "/" + apiPackage().replace('.', '/');
    }

    @Override
    public String apiFilename(String templateName, String tag) {
        String suffix = apiTemplateFiles().get(templateName);
        return apiFileFolder() + '/' + toApiFilename(tag) + suffix;
    }

    @Override
    public String apiTestFileFolder() {
        return outputFolder + "/" + testFolder + "/" + apiPackage().replace('.', '/');
    }

    @Override
    public String modelFileFolder() {
        return outputFolder + "/" + sourceFolder + "/" + modelPackage().replace('.', '/');
    }

    @Override
    public String apiDocFileFolder() {
        return (outputFolder + "/" + apiDocPath).replace('/', File.separatorChar);
    }

    @Override
    public String modelDocFileFolder() {
        return (outputFolder + "/" + modelDocPath).replace('/', File.separatorChar);
    }

    @Override
    public void processOpts() {
        super.processOpts();
        //supportingFiles.add(new SupportingFile("pom.mustache", "", "pom.xml"));
        supportingFiles.add(new SupportingFile("LICENSE.mustache", "", "LICENSE"));

        supportingFiles.add(new SupportingFile("README.mustache", "", "README.md"));
    }

    @Override
    public void preprocessSwagger(Swagger swagger) {
        if ( "/".equals(swagger.getBasePath()) ) {
            swagger.setBasePath("");
        }

        if (!this.additionalProperties.containsKey("serverPort")) {
            final String host = swagger.getHost();
            String port = "8080"; // Default value for a JEE Server
            if ( host != null ) {
                String[] parts = host.split(":");
                if ( parts.length > 1 ) {
                    port = parts[1];
                }
            }

            this.additionalProperties.put("serverPort", port);
        }

        if ( swagger.getPaths() != null ) {
            for ( String pathname : swagger.getPaths().keySet() ) {
                Path path = swagger.getPath(pathname);
                if ( path.getOperations() != null ) {
                    for ( Operation operation : path.getOperations() ) {
                        if ( operation.getTags() != null ) {
                            List<Map<String, String>> tags = new ArrayList<Map<String, String>>();
                            for ( String tag : operation.getTags() ) {
                                Map<String, String> value = new HashMap<String, String>();
                                value.put("tag", tag);
                                value.put("hasMore", "true");
                                tags.add(value);
                            }
                            if ( tags.size() > 0 ) {
                                tags.get(tags.size() - 1).remove("hasMore");
                            }
                            if ( operation.getTags().size() > 0 ) {
                                String tag = operation.getTags().get(0);
                                operation.setTags(Arrays.asList(tag));
                            }
                            operation.setVendorExtension("x-tags", tags);
                        }
                    }
                }
            }
        }
    }

//        if (swagger.getInfo() != null) {
//            Info info = swagger.getInfo();
//            if (info.getTitle() != null) {
//                // when info.title is defined, use it for projectName
//                // used in package.json
//                projectName = info.getTitle()
//                        .replaceAll("[^a-zA-Z0-9]", "-")
//                        .replaceAll("^[-]*", "")
//                        .replaceAll("[-]*$", "")
//                        .replaceAll("[-]{2,}", "-")
//                        .toLowerCase();
//                this.additionalProperties.put("projectName", projectName);
//            }
//
//            for (String pathname : swagger.getPaths().keySet()) {
//                Path path = swagger.getPath(pathname);
//                Map<HttpMethod, Operation> operationMap = path.getOperationMap();
//                if (operationMap != null) {
//                    for (HttpMethod method : operationMap.keySet()) {
//                        Operation operation = operationMap.get(method);
//                        if (operation.getTags() != null) operation.getTags().clear();
//                        String tags;
//                        if (pathname.endsWith("}")) {
//                            tags = pathname.replaceAll("/\\{([a-zA-Z]+)\\}", "") + "Path";
//                        } else {
//                            tags = pathname.replaceAll("/\\{([a-zA-Z]+)\\}", "") + "Query";
//                        }
//
//                        operation.setOperationId(method.toString().toUpperCase());
//                        //operation.setOperationId("handle" + method.toString().toUpperCase());
//                        LOGGER.info(tags);
//                        LOGGER.info(method.toString().toUpperCase());
//                        System.out.println("tags: "  + tags +"\n");
//                        System.out.println("method: " + method.toString().toUpperCase() +"\n");
//
//                        StringBuilder sb = new StringBuilder();
//                        for (String tag : tags.split("/")) {
//                            if (!tag.trim().isEmpty()) {
//                                sb.append(tag.substring(0, 1).toUpperCase() + tag.substring(1));
//                            }
//                        }
//                        LOGGER.info("processed tags: " + sb.toString());
//                        operation.addTag(sb.toString());
//                    }
//                }
//            }
//
//        }
//    }


    @Override
    public String escapeUnsafeCharacters(String input) {
        return input.replace("*/", "*_/").replace("/*", "/_*");
    }

    @Override
    public String escapeQuotationMark(String input) {
        // remove " to avoid code injection
        return input.replace("\"", "");
    }


}