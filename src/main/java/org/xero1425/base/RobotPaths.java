package org.xero1425.base ;

import edu.wpi.first.wpilibj.Filesystem;

class RobotPaths
{
    RobotPaths(boolean simulator, String name) {
        if (simulator)
        {
            log_file_directory_ = "logs" ;
            deploy_directory_ = "src/main/deploy/" ;
        }
        else
        {
            log_file_directory_ = "/u" ;
            deploy_directory_ = Filesystem.getDeployDirectory().getPath() + "/" ;
        }
    }

    String logFileDirectory() {
        return log_file_directory_ ;
    }

    String deployDirectory() {
        return deploy_directory_ ;
    }

    String pathsDirectory() {
        return deploy_directory_ + "/paths" ;
    }

    private String log_file_directory_ ;
    private String deploy_directory_ ;
} ;