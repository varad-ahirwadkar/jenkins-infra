def call(){
    script {
        ansiColor('xterm') {
            echo ""
        }
        try {
            sh '''
                echo 'Creating upgrade_vars.yaml'
                rm -rf ~/.ansible
                ansible all -m setup -a 'gather_subset=!all'
                cd ${WORKSPACE}/ocp4_playbooks
                cp examples/upgrade_vars.yaml upgrade_vars.yaml 
                sed -i "s|upgrade_image:.*$|upgrade_image: ${NEXT_UPGRADE_IMAGE}|g" upgrade_vars.yaml
                sed -i "s|delay_time:.*$|delay_time: 900|g" upgrade_vars.yaml
                cat upgrade_vars.yaml
                cp ${WORKSPACE}/ansible_extra/examples/inventory .
                sed -i "s|localhost|${BASTION_IP}|g" inventory
                sed -i 's/ansible_connection=local/ansible_connection=ssh/g' inventory
                sed -i "s|ssh|ssh ansible_ssh_private_key_file=${WORKSPACE}/deploy/id_rsa|g" inventory
                cat inventory
                echo "[ssh_connection]" >> ansible.cfg
                echo "ssh_args = -C -o ControlMaster=auto -o ControlPersist=120m -o ServerAliveInterval=30" >> ansible.cfg
                cat ansible.cfg
                ansible-playbook  -i inventory -e @upgrade_vars.yaml playbooks/upgrade.yaml
            '''
        }
        catch (err) {
            echo 'Error ! ansible Upgrade setup failed!'
            env.FAILED_STAGE=env.STAGE_NAME
            throw err
        }
    }
}
