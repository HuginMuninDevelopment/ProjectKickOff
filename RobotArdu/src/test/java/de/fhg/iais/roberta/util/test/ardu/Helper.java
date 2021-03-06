package de.fhg.iais.roberta.util.test.ardu;

import de.fhg.iais.roberta.components.Actor;
import de.fhg.iais.roberta.components.ActorType;
import de.fhg.iais.roberta.components.ArduConfiguration;
import de.fhg.iais.roberta.components.Configuration;
import de.fhg.iais.roberta.factory.ArduFactory;
import de.fhg.iais.roberta.mode.action.DriveDirection;
import de.fhg.iais.roberta.mode.action.MotorSide;
import de.fhg.iais.roberta.mode.action.arduino.ActorPort;

public class Helper extends de.fhg.iais.roberta.util.test.Helper {

    public Helper() {
        super();
        ArduFactory robotFactory = new ArduFactory(null);
        Configuration brickConfiguration =
            new ArduConfiguration.Builder()
                .addActor(ActorPort.A, new Actor(ActorType.LARGE, true, DriveDirection.FOREWARD, MotorSide.NONE))
                .addActor(ActorPort.B, new Actor(ActorType.MEDIUM, true, DriveDirection.FOREWARD, MotorSide.LEFT))
                .addActor(ActorPort.C, new Actor(ActorType.LARGE, false, DriveDirection.FOREWARD, MotorSide.RIGHT))
                .addActor(ActorPort.D, new Actor(ActorType.MEDIUM, false, DriveDirection.FOREWARD, MotorSide.NONE))
                .build();
        setRobotFactory(robotFactory);
        setRobotConfiguration(brickConfiguration);
    }
}
