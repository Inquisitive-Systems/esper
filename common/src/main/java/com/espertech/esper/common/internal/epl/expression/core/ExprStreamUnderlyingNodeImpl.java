/*
 ***************************************************************************************
 *  Copyright (C) 2006 EsperTech, Inc. All rights reserved.                            *
 *  http://www.espertech.com/esper                                                     *
 *  http://www.espertech.com                                                           *
 *  ---------------------------------------------------------------------------------- *
 *  The software in this package is published under the terms of the GPL license       *
 *  a copy of which has been included with this distribution in the license.txt file.  *
 ***************************************************************************************
 */
package com.espertech.esper.common.internal.epl.expression.core;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.EventType;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenClassScope;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethod;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethodScope;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionRef;
import com.espertech.esper.common.internal.epl.expression.codegen.ExprForgeCodegenSymbol;
import com.espertech.esper.common.internal.metrics.instrumentation.InstrumentationBuilderExpr;

import java.io.StringWriter;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.*;

/**
 * Represents an stream selector that returns the streams underlying event, or null if undefined.
 */
public class ExprStreamUnderlyingNodeImpl extends ExprNodeBase implements ExprForgeInstrumentable, ExprEvaluator, ExprStreamUnderlyingNode {
    private final String streamName;
    private final boolean isWildcard;
    private int streamNum = -1;
    private Class type;
    private transient EventType eventType;

    public ExprStreamUnderlyingNodeImpl(String streamName, boolean isWildcard) {
        if ((streamName == null) && (!isWildcard)) {
            throw new IllegalArgumentException("Stream name is null");
        }
        this.streamName = streamName;
        this.isWildcard = isWildcard;
    }

    public Class getEvaluationType() {
        if (streamNum == -1) {
            throw new IllegalStateException("Stream underlying node has not been validated");
        }
        return type;
    }

    public ExprEvaluator getExprEvaluator() {
        return this;
    }

    public ExprForge getForge() {
        return this;
    }

    public ExprNode getForgeRenderable() {
        return this;
    }

    /**
     * Returns the stream name.
     *
     * @return stream name
     */
    public String getStreamName() {
        return streamName;
    }

    public Integer getStreamReferencedIfAny() {
        return getStreamId();
    }

    public String getRootPropertyNameIfAny() {
        return null;
    }

    public ExprForgeConstantType getForgeConstantType() {
        return ExprForgeConstantType.NONCONST;
    }

    public ExprNode validate(ExprValidationContext validationContext) throws ExprValidationException {
        if (streamName == null && isWildcard) {
            if (validationContext.getStreamTypeService().getStreamNames().length > 1) {
                throw new ExprValidationException("Wildcard must be stream wildcard if specifying multiple streams, use the 'streamname.*' syntax instead");
            }
            streamNum = 0;
        } else {
            streamNum = validationContext.getStreamTypeService().getStreamNumForStreamName(streamName);
        }

        if (streamNum == -1) {
            throw new ExprValidationException("Stream by name '" + streamName + "' could not be found among all streams");
        }

        eventType = validationContext.getStreamTypeService().getEventTypes()[streamNum];
        type = eventType.getUnderlyingType();
        return null;
    }

    public boolean isConstantResult() {
        return false;
    }

    /**
     * Returns stream id supplying the property value.
     *
     * @return stream number
     */
    public int getStreamId() {
        if (streamNum == -1) {
            throw new IllegalStateException("Stream underlying node has not been validated");
        }
        return streamNum;
    }

    public String toString() {
        return "streamName=" + streamName +
                " streamNum=" + streamNum;
    }

    public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext) {
        EventBean event = eventsPerStream[streamNum];
        if (event == null) {
            return null;
        }
        return event.getUnderlying();
    }

    public CodegenExpression evaluateCodegenUninstrumented(Class requiredType, CodegenMethodScope codegenMethodScope, ExprForgeCodegenSymbol exprSymbol, CodegenClassScope codegenClassScope) {
        CodegenMethod methodNode = codegenMethodScope.makeChild(eventType.getUnderlyingType(), ExprStreamUnderlyingNodeImpl.class, codegenClassScope);
        CodegenExpressionRef refEPS = exprSymbol.getAddEPS(methodNode);
        methodNode.getBlock()
                .declareVar(EventBean.class, "event", arrayAtIndex(refEPS, constant(streamNum)))
                .ifRefNullReturnNull("event")
                .methodReturn(cast(eventType.getUnderlyingType(), exprDotMethod(ref("event"), "getUnderlying")));
        return localMethod(methodNode);
    }

    public CodegenExpression evaluateCodegen(Class requiredType, CodegenMethodScope codegenMethodScope, ExprForgeCodegenSymbol exprSymbol, CodegenClassScope codegenClassScope) {
        return new InstrumentationBuilderExpr(this.getClass(), this, "ExprStreamUnd", requiredType, codegenMethodScope, exprSymbol, codegenClassScope).build();
    }

    public void toPrecedenceFreeEPL(StringWriter writer) {
        writer.append(streamName);
        if (isWildcard) {
            writer.append(".*");
        }
    }

    public ExprPrecedenceEnum getPrecedence() {
        return ExprPrecedenceEnum.UNARY;
    }

    public EventType getEventType() {
        return eventType;
    }

    public boolean equalsNode(ExprNode node, boolean ignoreStreamPrefix) {
        if (!(node instanceof ExprStreamUnderlyingNodeImpl)) {
            return false;
        }

        ExprStreamUnderlyingNodeImpl other = (ExprStreamUnderlyingNodeImpl) node;
        if (this.isWildcard != other.isWildcard) {
            return false;
        }
        if (this.isWildcard) {
            return true;
        }
        return this.streamName.equals(other.streamName);
    }
}
