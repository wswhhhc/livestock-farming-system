const STAGE_LABELS = ['', '苗种', '青年', '成年', '出栏前']
const STAGE_TAG_TYPES = ['', 'warning', 'primary', 'success', 'danger']

export function stageLabel(stage) {
  return STAGE_LABELS[stage] || ''
}

export function stageTagType(stage) {
  return STAGE_TAG_TYPES[stage] || ''
}
