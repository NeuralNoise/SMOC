/**
 * 
 */
package bg.smoc.model;

/**
 * @author tsvetan.bogdanov@gmail.com
 * 
 *         Stores the contest state - are tests enabled, are tests enabled, is
 *         contest in analysis mode, etc.
 */
public class ContestState {

    private boolean inAnalysisMode;

    public boolean isInAnalysisMode() {
        return inAnalysisMode;
    }

    public void setInAnalysisMode(boolean inAnalysisMode) {
        this.inAnalysisMode = inAnalysisMode;
    }

}
