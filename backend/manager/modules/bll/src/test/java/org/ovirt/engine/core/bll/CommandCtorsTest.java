package org.ovirt.engine.core.bll;

import java.util.Collection;
import java.util.stream.Stream;

import org.junit.BeforeClass;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.compat.Guid;

public class CommandCtorsTest extends CtorsTestBase {

    private static Collection<Class<?>> commandClasses;

    @BeforeClass
    public static void initCommandsCollection() {
        commandClasses = commandsFromEnum(VdcActionType.class, CommandsFactory::getCommandClass);
    }

    @Override
    protected Collection<Class<?>> getClassesToTest() {
        return commandClasses;
    }

    @Override
    protected Stream<MandatoryCtorPredicate> getMandatoryCtorPredicates() {
        return Stream.of(new MandatoryCtorPredicate(VdcActionParametersBase.class, CommandContext.class),
                new MandatoryCtorPredicate(Guid.class) {
                    @Override
                    public boolean test(Class<?> commandClass) {
                        return !commandClass.isAnnotationPresent(NonTransactiveCommandAttribute.class) ||
                                !commandClass.getAnnotation(NonTransactiveCommandAttribute.class).forceCompensation() ||
                                super.test(commandClass);
                    }

                    @Override
                    protected String additionalInfo() {
                        return "its class is annotated with '" + NonTransactiveCommandAttribute.class.getSimpleName() +
                                "' and the annotation's 'forceCompensation' attribute is set to true.";
                    }
                });
    }
}
