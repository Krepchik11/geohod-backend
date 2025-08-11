### One-Time Server Setup

0.  **Install Podman:** Ensure `podman` is installed on the VPS.

1.  **Create Deploy User & Directories:**
    ```bash
    # As root or with sudo
    useradd -m -s /bin/bash vps-user
    mkdir -p /home/vps-user/geohod-backend-dev
    chown -R vps-user:vps-user /home/vps-user/geohod-backend-dev
    ```

2.  **Configure Sudo:** The deploy user needs passwordless `sudo` access **only** for the installer script. Run `sudo visudo` and add the following line, replacing `vps-user` with the correct username:

    ```
    vps-user ALL=(root) NOPASSWD: /home/vps-user/geohod-backend-dev/staging/install.sh
    ```

3.  **SSH Access:** Add the public key corresponding to the `VPS_SSH_KEY` GitHub secret to the `~/.ssh/authorized_keys` file for the `vps-user`.

### Running a Deployment

1.  Ensure all required secrets (`VPS_USER`, `VPS_SSH_KEY`, etc.) and variables (`VPS_HOST`) are configured in the GitHub repository settings under **Settings > Secrets and variables > Actions**.
2.  Go to the **Actions** tab in the GitHub repository.
3.  Select the **Deploy to Dev** workflow from the list.
4.  Click the **Run workflow** button to trigger a manual deployment.