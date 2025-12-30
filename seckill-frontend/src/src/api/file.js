import http from '../utils/http'

/**
 * 上传图片文件
 * @param {File} file - 要上传的图片文件
 * @returns {Promise<string>} - 返回图片的访问URL
 */
export const uploadImage = async (file) => {
  // 检查文件是否存在且有效
  if (!file || !(file instanceof File)) {
    throw new Error('无效的图片文件')
  }
  
  const formData = new FormData()
  formData.append('file', file)
  
  const response = await http.post('/api/upload/image', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
  
  return response.data
}