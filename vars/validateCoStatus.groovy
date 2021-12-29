def call() {
    script {
        ansiColor('xterm') {
            echo ""
        }
        try {
            // Get Cluster Operators status
            sh '''#!/bin/bash
                echo 'oc version'
                oc version
                echo 'Setting up kubectl!'
                oc get nodes
                cd ${WORKSPACE}
                #echo "Gathering logs"
                #ssh -o 'StrictHostKeyChecking no' -i ${WORKSPACE}/deploy/id_rsa root@${BASTION_IP} "oc adm must-gather; tar -czf must-gather.tar.gz ./must-gather*"
                #scp -i ${WORKSPACE}/deploy/id_rsa -o StrictHostKeyChecking=no root@${BASTION_IP}:~/must-gather.tar.gz .
                sleep_time=300
                count=0
                for((i=0;i<18;++i)) do
                    count=$(oc get co --no-headers | awk '{ print $3 $4 $5 }' | grep -w -v TrueFalseFalse | wc -l)
                    if [ $count -ne 0 ]; then
                        echo "sleeping for 5 mins all co are not up"
                        sleep $sleep_time
                    else
                        echo "All cluster operators are up and running"
                        echo "All cluster operators were up and running" > ${WORKSPACE}/co_status.txt
                        count=0
                        oc get co
                        break
                    fi
                done
                if [ $count -ne 0 ]; then
                    oc get co
                    echo "Cluster operators were in degraded state after 90 mins" > ${WORKSPACE}/co_status.txt
                    echo "Cluster operators are in degraded state after 90 mins Tearing off cluster!!"
                    exit 1
                fi
                '''
        }
        catch (err) {
            echo 'Error ! All Cluster Operators are not Up .. Skipping e2e  !'
            env.FAILED_STAGE=env.STAGE_NAME
            throw err
        }
    }
}
