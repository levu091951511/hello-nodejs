import java.io.IOException;
import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.expression.ExpressionLanguageScope;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.*;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Tags({"example"})
@CapabilityDescription("Hello World to output")
@SeeAlso({})
@ReadsAttributes({@ReadsAttribute(attribute = "", description = "")})
@WritesAttributes({@WritesAttribute(attribute = "", description = "")})
public class CDCProcessor extends AbstractProcessor {

    public static final PropertyDescriptor propertyDescriptor = new PropertyDescriptor
            .Builder().name("name")
            .displayName("name")
            .description("Name to print")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .expressionLanguageSupported(ExpressionLanguageScope.FLOWFILE_ATTRIBUTES)
            .build();

    public static final Relationship successRelations = new Relationship.Builder()
            .name("success")
            .description("If everything is ok")
            .build();

    public static final Relationship failureRelations = new Relationship.Builder()
            .name("failure")
            .description("If something is wrong")
            .build();

    private List<PropertyDescriptor> descriptorList;

    private Set<Relationship> relationshipSet;

    @Override
    protected void init(final ProcessorInitializationContext context) {
        final List<PropertyDescriptor> descriptors = new ArrayList<PropertyDescriptor>();
        descriptors.add(propertyDescriptor);
        this.descriptorList = Collections.unmodifiableList(descriptors);

        final Set<Relationship> relationships = new HashSet<Relationship>();
        relationships.add(successRelations);
        relationships.add(failureRelations);
        this.relationshipSet = Collections.unmodifiableSet(relationships);
    }

    public Set<Relationship> getRelationshipSet() {
        return this.relationshipSet;
    }

    @Override
    public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return descriptorList;
    }

    @OnScheduled
    public void onScheduled(final ProcessContext context) {

    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        FlowFile flowFile = session.get();
        if (flowFile == null) {
            return;
        }

        try {
            String name = context.getProperty(propertyDescriptor).evaluateAttributeExpressions(flowFile).getValue();

            String result = "Hello " + name;
            session.putAttribute(flowFile, "result", result);

            try (OutputStream flowFileOutputStream = session.write(flowFile)) {
                flowFileOutputStream.write(result.getBytes(StandardCharsets.UTF_8));
            }

            session.transfer(flowFile, successRelations);
        } catch (IOException e) {
		        //IO Error processing error log
		        session.transfer(flowFile, failureRelations);
		    } catch (ProcessException e) {
				    // Process error log
		        getLogger().error("Processing error", e);
		        session.transfer(flowFile, failureRelations);
		    }
    }
}