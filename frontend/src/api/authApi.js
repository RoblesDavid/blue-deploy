import apiClient from '../services/apiClient';

export const authApi = {
  /**
   * Autenticación contra el backend de Spring Boot.
   * @param {Object} credentials - { username, password }
   * @returns {Promise<Object>} LoginResponseDTO con token y perfil
   */
  async login(credentials) {
    return await apiClient.post('/auth/login', credentials);
  },
};

export default authApi;
