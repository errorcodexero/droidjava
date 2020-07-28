package org.frc2020.droid.blinky ;

import org.xero1425.base.actions.Action;

public class BlinkySetPatternAction extends Action
{
    public enum Pattern {
        Off(0),
        FiveBalls(1),
        ThirtySeconds(2),
        TraverseLeft(3),
        TraverseRight(4),
        FunPatternOne(5) ;

        public final int value ;
        private Pattern(int value) {
            this.value = value ;
        }
    } ;

    public BlinkySetPatternAction(BlinkySubsystem sub, Pattern p) {
        super(sub.getRobot().getMessageLogger()) ;

        sub_ = sub ;
        pattern_ = p ;
    }

    @Override
    public void start() throws Exception {
        super.start() ;
        sub_.setPattern(pattern_.value);
        setDone() ;
    }

    @Override
    public void run() {
    }

    @Override
    public String toString(int indent) {
        return prefix(indent) + "BlinkySetPatternAction " + pattern_.toString() ;
    }

    private BlinkySubsystem sub_ ;
    private Pattern pattern_ ;
}